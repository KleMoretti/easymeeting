import { create } from 'zustand';
import type { UserInfoVO } from '../types/api';

const STORAGE_KEY = 'easymeeting-auth';

interface AuthState {
  user?: UserInfoVO;
  token?: string;
  setUser: (user: UserInfoVO) => void;
  logout: () => void;
  restore: () => void;
}

function readStoredUser() {
  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return undefined;
  }
  try {
    return JSON.parse(raw) as UserInfoVO;
  } catch {
    window.localStorage.removeItem(STORAGE_KEY);
    return undefined;
  }
}

export const useAuthStore = create<AuthState>((set) => ({
  user: readStoredUser(),
  token: readStoredUser()?.token,
  setUser: (user) => {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
    set({ user, token: user.token });
  },
  logout: () => {
    window.localStorage.removeItem(STORAGE_KEY);
    set({ user: undefined, token: undefined });
  },
  restore: () => {
    const user = readStoredUser();
    set({ user, token: user?.token });
  },
}));
