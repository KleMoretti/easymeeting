export interface ResponseVO<T> {
  status: 'success' | 'error';
  code: number;
  info: string;
  data: T;
}

export interface CheckCodeVO {
  checkCode: string;
  checkCodeKey: string;
}

export interface UserInfoVO {
  userId: string;
  nickName: string;
  sex?: number;
  token: string;
  meetingNo: string;
  admin: boolean;
}
