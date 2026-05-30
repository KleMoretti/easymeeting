import { describe, expect, it } from 'vitest';
import { buildWebSocketUrl } from './meetingSocket';

describe('buildWebSocketUrl', () => {
  it('uses current host and appends the token', () => {
    const loc = {
      protocol: 'http:',
      host: 'localhost:5173',
    } as Location;

    expect(buildWebSocketUrl('a b', loc)).toBe('ws://localhost:5173/ws?token=a%20b');
  });

  it('uses wss for https pages', () => {
    const loc = {
      protocol: 'https:',
      host: 'meeting.example.com',
    } as Location;

    expect(buildWebSocketUrl('token', loc)).toBe('wss://meeting.example.com/ws?token=token');
  });
});
