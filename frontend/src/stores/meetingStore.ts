import { create } from 'zustand';
import { exitMeeting, joinMeeting, updateMediaStatus } from '../api/meeting';
import type { UserInfoVO } from '../types/api';
import type {
  MeetingExitDto,
  MeetingJoinDto,
  MeetingMediaStatusDto,
  MeetingMemberDto,
} from '../types/meeting';
import { MessageType, type MessageSendDto, type PeerMessageDto } from '../types/ws';
import { MeetingSocket, type SocketStatus } from '../ws/meetingSocket';
import { MeshRtcManager } from '../webrtc/meshManager';

interface StartMeetingParams {
  token: string;
  user: UserInfoVO;
  meetingId: string;
  videoOpen: boolean;
  audioOpen: boolean;
}

interface MeetingState {
  meetingId?: string;
  members: MeetingMemberDto[];
  localStream?: MediaStream;
  remoteStreams: Record<string, MediaStream>;
  socketStatus: SocketStatus;
  videoOpen: boolean;
  audioOpen: boolean;
  starting: boolean;
  startMeetingSession: (params: StartMeetingParams) => Promise<void>;
  toggleAudio: () => Promise<void>;
  toggleVideo: () => Promise<void>;
  leaveMeeting: () => Promise<void>;
  cleanup: () => void;
}

let socket: MeetingSocket | undefined;
let rtc: MeshRtcManager | undefined;
let currentUserId: string | undefined;

function parseContent<T>(content: unknown): T {
  if (typeof content === 'string') {
    return JSON.parse(content) as T;
  }
  return content as T;
}

export const useMeetingStore = create<MeetingState>((set, get) => {
  const handleMessage = async (message: MessageSendDto) => {
    if (message.messageType === MessageType.Peer) {
      if (message.sendUserId === currentUserId) {
        return;
      }
      const peerMessage = parseContent<PeerMessageDto>(message.messageContent);
      await rtc?.handleSignal(
        message.sendUserId,
        peerMessage.signalType,
        peerMessage.signalData,
      );
      return;
    }

    if (message.messageType === MessageType.AddMeetingRoom) {
      const joinDto = parseContent<MeetingJoinDto>(message.messageContent);
      set({ members: joinDto.meetingMemberList ?? [] });
      if (joinDto.newMember?.userId && joinDto.newMember.userId !== currentUserId) {
        await rtc?.createOfferFor(joinDto.newMember.userId);
      }
      return;
    }

    if (message.messageType === MessageType.ExitMeetingRoom) {
      const exitDto = parseContent<MeetingExitDto>(message.messageContent);
      set({ members: exitDto.meetingMemberList ?? [] });
      if (exitDto.exitUserId) {
        rtc?.removePeer(exitDto.exitUserId);
      }
      return;
    }

    if (
      message.messageType === MessageType.MeetingUserVideoChange ||
      message.messageType === MessageType.MeetingUserAudioChange ||
      message.messageType === MessageType.MeetingUserMediaChange
    ) {
      const mediaDto = parseContent<MeetingMediaStatusDto>(message.messageContent);
      set({ members: mediaDto.meetingMemberList ?? [] });
      return;
    }

    if (message.messageType === MessageType.FinishMeeting) {
      get().cleanup();
    }
  };

  return {
    members: [],
    remoteStreams: {},
    socketStatus: 'idle',
    videoOpen: true,
    audioOpen: true,
    starting: false,

    startMeetingSession: async ({ token, user, meetingId, videoOpen, audioOpen }) => {
      get().cleanup();
      currentUserId = user.userId;
      set({ starting: true, meetingId, videoOpen, audioOpen });

      rtc = new MeshRtcManager({
        onRemoteStream: (userId, stream) => {
          set((state) => ({
            remoteStreams: { ...state.remoteStreams, [userId]: stream },
          }));
        },
        onRemoteStreamRemoved: (userId) => {
          set((state) => {
            const nextStreams = { ...state.remoteStreams };
            delete nextStreams[userId];
            return { remoteStreams: nextStreams };
          });
        },
        sendSignal: (receiveUserId, signalType, signalData) => {
          socket?.sendSignal({ receiveUserId, signalType, signalData });
        },
      });

      const localStream = await rtc.initLocalMedia(videoOpen, audioOpen);
      socket = new MeetingSocket(token, handleMessage, (socketStatus) => {
        set({ socketStatus });
      });
      await socket.connect();
      await joinMeeting(videoOpen, audioOpen);
      set({ localStream, starting: false });
    },

    toggleAudio: async () => {
      const next = !get().audioOpen;
      rtc?.setTrackEnabled('audio', next);
      set({ audioOpen: next });
      await updateMediaStatus({ audioOpen: next });
    },

    toggleVideo: async () => {
      const next = !get().videoOpen;
      rtc?.setTrackEnabled('video', next);
      set({ videoOpen: next });
      await updateMediaStatus({ videoOpen: next });
    },

    leaveMeeting: async () => {
      try {
        await exitMeeting();
      } finally {
        get().cleanup();
      }
    },

    cleanup: () => {
      socket?.close();
      rtc?.close();
      socket = undefined;
      rtc = undefined;
      currentUserId = undefined;
      set({
        meetingId: undefined,
        members: [],
        localStream: undefined,
        remoteStreams: {},
        socketStatus: 'idle',
        videoOpen: true,
        audioOpen: true,
        starting: false,
      });
    },
  };
});
