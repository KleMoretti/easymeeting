import type { SignalType } from '../types/ws';

interface MeshRtcManagerOptions {
  onRemoteStream: (userId: string, stream: MediaStream) => void;
  onRemoteStreamRemoved: (userId: string) => void;
  sendSignal: (receiveUserId: string, signalType: SignalType, signalData: string) => void;
}

export class MeshRtcManager {
  private localStream?: MediaStream;
  private readonly peers = new Map<string, RTCPeerConnection>();
  private readonly remoteStreams = new Map<string, MediaStream>();

  constructor(private readonly options: MeshRtcManagerOptions) {}

  getLocalStream() {
    return this.localStream;
  }

  async initLocalMedia(videoOpen: boolean, audioOpen: boolean) {
    this.localStream = await navigator.mediaDevices.getUserMedia({
      video: videoOpen,
      audio: audioOpen,
    });
    return this.localStream;
  }

  setTrackEnabled(kind: 'audio' | 'video', enabled: boolean) {
    const tracks =
      kind === 'audio'
        ? this.localStream?.getAudioTracks()
        : this.localStream?.getVideoTracks();
    tracks?.forEach((track) => {
      track.enabled = enabled;
    });
  }

  async createOfferFor(userId: string) {
    const peer = this.ensurePeer(userId);
    const offer = await peer.createOffer();
    await peer.setLocalDescription(offer);
    this.options.sendSignal(userId, 'offer', JSON.stringify(offer));
  }

  async handleSignal(userId: string, signalType: SignalType, signalData: string) {
    const peer = this.ensurePeer(userId);
    const data = JSON.parse(signalData);

    if (signalType === 'offer') {
      await peer.setRemoteDescription(new RTCSessionDescription(data));
      const answer = await peer.createAnswer();
      await peer.setLocalDescription(answer);
      this.options.sendSignal(userId, 'answer', JSON.stringify(answer));
      return;
    }

    if (signalType === 'answer') {
      await peer.setRemoteDescription(new RTCSessionDescription(data));
      return;
    }

    if (signalType === 'candidate') {
      await peer.addIceCandidate(new RTCIceCandidate(data));
    }
  }

  removePeer(userId: string) {
    this.peers.get(userId)?.close();
    this.peers.delete(userId);
    this.remoteStreams.delete(userId);
    this.options.onRemoteStreamRemoved(userId);
  }

  close() {
    this.peers.forEach((peer) => peer.close());
    this.peers.clear();
    this.remoteStreams.clear();
    this.localStream?.getTracks().forEach((track) => track.stop());
    this.localStream = undefined;
  }

  private ensurePeer(userId: string) {
    const existingPeer = this.peers.get(userId);
    if (existingPeer) {
      return existingPeer;
    }

    const peer = new RTCPeerConnection({
      iceServers: [{ urls: 'stun:stun.l.google.com:19302' }],
    });

    this.localStream?.getTracks().forEach((track) => {
      if (this.localStream) {
        peer.addTrack(track, this.localStream);
      }
    });

    peer.onicecandidate = (event) => {
      if (event.candidate) {
        this.options.sendSignal(
          userId,
          'candidate',
          JSON.stringify(event.candidate),
        );
      }
    };

    peer.ontrack = (event) => {
      const [stream] = event.streams;
      if (!stream) {
        return;
      }
      this.remoteStreams.set(userId, stream);
      this.options.onRemoteStream(userId, stream);
    };

    this.peers.set(userId, peer);
    return peer;
  }
}
