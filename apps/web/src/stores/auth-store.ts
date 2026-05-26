import { create } from "zustand";

import { getMe } from "@/features/auth/api/auth-api";
import {
  deleteAccessTokenCookie,
  getAccessTokenCookie,
  setAccessTokenCookie,
} from "@/lib/cookie";
import type { User } from "@/types/auth";

type AuthState = {
  user: User | null;
  isInitialized: boolean;
  isAuthenticated: boolean;
  initializeAuth: () => Promise<void>;
  setAuth: (accessToken: string, user: User) => void;
  setUser: (user: User) => void;
  clearUser: () => void;
};

let authInitializationPromise: Promise<void> | null = null;

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  isInitialized: false,
  isAuthenticated: false,
  initializeAuth: async () => {
    if (get().isInitialized) {
      return;
    }

    if (!getAccessTokenCookie()) {
      set({ user: null, isAuthenticated: false, isInitialized: true });
      return;
    }

    if (authInitializationPromise) {
      return authInitializationPromise;
    }

    authInitializationPromise = getMe()
      .then((user) => {
        set({ user, isAuthenticated: true, isInitialized: true });
      })
      .catch(() => {
        deleteAccessTokenCookie();
        set({ user: null, isAuthenticated: false, isInitialized: true });
      })
      .finally(() => {
        authInitializationPromise = null;
      });

    return authInitializationPromise;
  },
  setAuth: (accessToken, user) => {
    setAccessTokenCookie(accessToken);
    set({ user, isAuthenticated: true, isInitialized: true });
  },
  setUser: (user) => {
    set({ user, isAuthenticated: true, isInitialized: true });
  },
  clearUser: () => {
    deleteAccessTokenCookie();
    set({ user: null, isAuthenticated: false, isInitialized: true });
  },
}));
