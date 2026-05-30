export interface MeetingInfo {
  meetingId: string;
  meetingNo: string;
  meetingName: string;
  createUserId: string;
  joinType: number;
  joinPassword?: string;
  startTime?: string;
  endTime?: string;
  status: number;
}

export interface MeetingMemberDto {
  openVideo: boolean;
  openAudio: boolean;
  avatar?: string;
  nickName: string;
  sex?: number;
  joinTime: number;
  status: number;
  userId: string;
  memberType: number;
}

export interface MeetingJoinDto {
  newMember: MeetingMemberDto;
  meetingMemberList: MeetingMemberDto[];
}

export interface MeetingExitDto {
  exitUserId: string;
  meetingMemberList: MeetingMemberDto[];
}

export interface MeetingMediaStatusDto {
  meetingId: string;
  userId: string;
  openVideo?: boolean;
  openAudio?: boolean;
  meetingMemberList: MeetingMemberDto[];
}
