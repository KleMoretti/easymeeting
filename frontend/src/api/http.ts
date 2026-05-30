import type { ResponseVO } from '../types/api';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '/api';

let tokenProvider: (() => string | undefined) | undefined;
let unauthorizedHandler: (() => void) | undefined;

export function setTokenProvider(provider: () => string | undefined) {
  tokenProvider = provider;
}

export function setUnauthorizedHandler(handler: () => void) {
  unauthorizedHandler = handler;
}

export class ApiError extends Error {
  constructor(
    public readonly code: number,
    message: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export function toFormBody(params: object): URLSearchParams {
  const body = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      body.append(key, String(value));
    }
  });
  return body;
}

export async function request<T>(
  path: string,
  params: object = {},
): Promise<T> {
  const headers = new Headers({
    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
  });
  const token = tokenProvider?.();
  if (token) {
    headers.set('token', token);
  }

  const response = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers,
    body: toFormBody(params),
  });

  if (!response.ok) {
    throw new ApiError(response.status, `请求失败：${response.status}`);
  }

  const payload = (await response.json()) as ResponseVO<T>;
  if (payload.code === 901) {
    unauthorizedHandler?.();
  }
  if (payload.status !== 'success' || payload.code !== 200) {
    throw new ApiError(payload.code, payload.info || '请求失败');
  }
  return payload.data;
}
