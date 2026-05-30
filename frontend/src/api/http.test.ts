import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiError, request, setTokenProvider, setUnauthorizedHandler } from './http';

describe('request', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    setTokenProvider(() => 'token-1');
  });

  it('sends form-urlencoded POST requests with token header', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ status: 'success', code: 200, info: 'ok', data: 'done' }),
    });
    vi.stubGlobal('fetch', fetchMock);

    await expect(request('/meeting/joinMeeting', { videoOpen: true })).resolves.toBe('done');

    const [, init] = fetchMock.mock.calls[0];
    expect(init.method).toBe('POST');
    expect(init.headers.get('token')).toBe('token-1');
    expect(init.body.toString()).toBe('videoOpen=true');
  });

  it('runs unauthorized handler for code 901', async () => {
    const handler = vi.fn();
    setUnauthorizedHandler(handler);
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        json: async () => ({ status: 'error', code: 901, info: 'timeout', data: null }),
      }),
    );

    await expect(request('/meeting/loadMeeting')).rejects.toBeInstanceOf(ApiError);
    expect(handler).toHaveBeenCalledTimes(1);
  });
});
