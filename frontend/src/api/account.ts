import { request } from './http';
import type { CheckCodeVO, UserInfoVO } from '../types/api';

export function loadCheckCode() {
  return request<CheckCodeVO>('/account/checkCode');
}

export interface LoginParams {
  checkCodeKey: string;
  email: string;
  password: string;
  checkCode: string;
}

export function login(params: LoginParams) {
  return request<UserInfoVO>('/account/login', params);
}

export interface RegisterParams extends LoginParams {
  nickName: string;
}

export function register(params: RegisterParams) {
  return request<null>('/account/register', params);
}
