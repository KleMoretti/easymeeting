export enum MessageType {
  Init = 0,
  AddMeetingRoom = 1,
  Peer = 2,
  ExitMeetingRoom = 3,
  FinishMeeting = 4,
  ChatTextMessage = 5,
  ChatMediaMessage = 6,
  ChatMediaMessageUpdate = 7,
  UserContactApply = 8,
  InviteMemberMeeting = 9,
  ForceOffline = 10,
  MeetingUserVideoChange = 11,
  MeetingUserAudioChange = 12,
  MeetingUserMediaChange = 13,
}

export interface MessageSendDto<T = unknown> {
  messageSend2Type: number;
  meetingId: string;
  messageType: MessageType;
  sendUserId: string;
  sendUserNickName?: string;
  messageContent: T;
  receiveUserId?: string;
  sendTime?: number;
  messageId?: number;
  status?: number;
}

export type SignalType = 'offer' | 'answer' | 'candidate';

export interface PeerMessageDto {
  signalType: SignalType;
  signalData: string;
}

export interface PeerConnectionDataDto {
  token: string;
  sendUserId?: string;
  receiveUserId?: string;
  signalType: SignalType;
  signalData: string;
}
