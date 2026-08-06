import { create } from "zustand";
import { User, Notification } from "../types";

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  setUser: (user: User | null) => void;
  setToken: (token: string) => void;
  setLoading: (isLoading: boolean) => void;
  setError: (error: string | null) => void;
  logout: () => void;
  reset: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,

  setUser: (user) =>
    set({
      user,
      isAuthenticated: user !== null,
    }),

  setToken: (token) =>
    set({
      token,
      isAuthenticated: !!token,
    }),

  setLoading: (isLoading) => set({ isLoading }),

  setError: (error) => set({ error }),

  logout: () =>
    set({
      user: null,
      token: null,
      isAuthenticated: false,
      error: null,
    }),

  reset: () =>
    set({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
    }),
}));

export interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  isLoading: boolean;

  setNotifications: (notifications: Notification[]) => void;
  addNotification: (notification: Notification) => void;
  removeNotification: (id: number) => void;
  setUnreadCount: (count: number) => void;
  incrementUnreadCount: () => void;
  decrementUnreadCount: () => void;
  markAsRead: (id: number) => void;
  markAllAsRead: () => void;
  setLoading: (isLoading: boolean) => void;
  reset: () => void;
}

export const useNotificationStore = create<NotificationState>((set) => ({
  notifications: [],
  unreadCount: 0,
  isLoading: false,

  setNotifications: (notifications) => {
    const unreadCount = notifications.filter((n) => !n.read).length;
    set({ notifications, unreadCount });
  },

  addNotification: (notification) =>
    set((state) => {
      const updated = [notification, ...state.notifications];
      const unreadCount = updated.filter((n) => !n.read).length;
      return { notifications: updated, unreadCount };
    }),

  removeNotification: (id) =>
    set((state) => {
      const updated = state.notifications.filter((n) => n.id !== id);
      const unreadCount = updated.filter((n) => !n.read).length;
      return { notifications: updated, unreadCount };
    }),

  setUnreadCount: (count) => set({ unreadCount: count }),

  incrementUnreadCount: () =>
    set((state) => ({ unreadCount: state.unreadCount + 1 })),

  decrementUnreadCount: () =>
    set((state) => ({ unreadCount: Math.max(0, state.unreadCount - 1) })),

  markAsRead: (id) =>
    set((state) => {
      const updated = state.notifications.map((n) =>
        n.id === id ? { ...n, read: true } : n
      );
      const unreadCount = updated.filter((n) => !n.read).length;
      return { notifications: updated, unreadCount };
    }),

  markAllAsRead: () =>
    set((state) => ({
      notifications: state.notifications.map((n) => ({ ...n, read: true })),
      unreadCount: 0,
    })),

  setLoading: (isLoading) => set({ isLoading }),

  reset: () =>
    set({
      notifications: [],
      unreadCount: 0,
      isLoading: false,
    }),
}));
