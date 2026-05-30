import type { MessageSendDto, PeerConnectionDataDto } from '../types/ws';

export type SocketStatus = 'idle' | 'connecting' | 'open' | 'closed' | 'error';

const HEARTBEAT_INTERVAL_MS = 4000;

export function buildWebSocketUrl(token: string, loc: Location = window.location) {
  const explicitUrl = import.meta.env.VITE_WS_URL as string | undefined;
  if (explicitUrl) {
    const separator = explicitUrl.includes('?') ? '&' : '?';
    return `${explicitUrl}${separator}token=${encodeURIComponent(token)}`;
  }
  const protocol = loc.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${protocol}//${loc.host}/ws?token=${encodeURIComponent(token)}`;
}

export class MeetingSocket {
  private socket?: WebSocket;
  private heartbeatId?: number;

  constructor(
    private readonly token: string,
    private readonly onMessage: (message: MessageSendDto) => void,
    private readonly onStatusChange: (status: SocketStatus) => void,
  ) {}

  connect(): Promise<void> {
    this.close();
    this.onStatusChange('connecting');
    return new Promise((resolve, reject) => {
      const socket = new WebSocket(buildWebSocketUrl(this.token));
      this.socket = socket;

      socket.onopen = () => {
        this.onStatusChange('open');
        this.startHeartbeat();
        resolve();
      };
      socket.onerror = () => {
        this.onStatusChange('error');
        reject(new Error('WebSocket 连接失败'));
      };
      socket.onclose = () => {
        this.stopHeartbeat();
        this.onStatusChange('closed');
      };
      socket.onmessage = (event) => {
        if (typeof event.data !== 'string') {
          return;
        }
        try {
          this.onMessage(JSON.parse(event.data) as MessageSendDto);
        } catch {
          // Ignore malformed frames from stale connections.
        }
      };
    });
  }

  sendSignal(payload: Omit<PeerConnectionDataDto, 'token'>) {
    if (this.socket?.readyState !== WebSocket.OPEN) {
      throw new Error('WebSocket 未连接');
    }
    this.socket.send(JSON.stringify({ ...payload, token: this.token }));
  }

  close() {
    this.stopHeartbeat();
    if (this.socket && this.socket.readyState !== WebSocket.CLOSED) {
      this.socket.close();
    }
    this.socket = undefined;
  }

  private startHeartbeat() {
    this.stopHeartbeat();
    this.heartbeatId = window.setInterval(() => {
      if (this.socket?.readyState === WebSocket.OPEN) {
        this.socket.send('ping');
      }
    }, HEARTBEAT_INTERVAL_MS);
  }

  private stopHeartbeat() {
    if (this.heartbeatId) {
      window.clearInterval(this.heartbeatId);
      this.heartbeatId = undefined;
    }
  }
}
