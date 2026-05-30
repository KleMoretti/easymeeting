import { request } from './http';
import type { MeetingInfo } from '../types/meeting';

export interface QuickMeetingParams {
  meetingNoType: number;
  meetingName: string;
  joinType: number;
  joinPassword?: string;
}

export function quickMeeting(params: QuickMeetingParams) {
  return request<string>('/meeting/quickMeeting', params);
}

export interface PreJoinMeetingParams {
  meetingNo: string;
  nickName: string;
  password?: string;
}

export function preJoinMeeting(params: PreJoinMeetingParams) {
  return request<string>('/meeting/preJoinMeeting', params);
}

export function joinMeeting(videoOpen: boolean, audioOpen: boolean) {
  return request<null>('/meeting/joinMeeting', { videoOpen, audioOpen });
}

export function updateMediaStatus(params: {
  videoOpen?: boolean;
  audioOpen?: boolean;
}) {
  return request<null>('/meeting/updateMediaStatus', params);
}

export function exitMeeting() {
  return request<null>('/meeting/exitMeeting');
}

export function finishMeeting() {
  return request<null>('/meeting/finishMeeting');
}

export function getCurrentMeeting(userId: string) {
  return request<MeetingInfo | null>('/meeting/getCurrentMeeting', { userId });
}
