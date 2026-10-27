import { api, type PageResponse } from './api';
import type { AppNotification } from '../types';

export const notificationService = {
  getNotifications: (page = 0, size = 15) =>
    api.get<PageResponse<AppNotification>>(`/notifications?page=${page}&size=${size}`),

  getUnreadCount: async (): Promise<number> => {
    const res = await api.get<{ unreadCount: number }>('/notifications/unread-count');
    return res.unreadCount;
  },

  markAsRead: (id: number) =>
    api.patch<void>(`/notifications/${id}/read`),

  createEventSource: (clientId: string): EventSource => {
    const apiBase = import.meta.env.VITE_API_BASE_URL || '/api/v1';
    const url = `${apiBase}/notifications/stream?clientId=${encodeURIComponent(clientId)}`;
    return new EventSource(url);
  }
};
