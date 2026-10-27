import { api } from './api';
import type { AuthResponse, User } from '../types';

export const authService = {
  login: async (email: string, password: string):Promise<AuthResponse> => {
    const res = await api.post<AuthResponse>('/auth/login', { email, password });
    localStorage.setItem('lifelink_token', res.token);
    localStorage.setItem('lifelink_user', JSON.stringify(res.user));
    return res;
  },

  registerHospital: async (data: any): Promise<AuthResponse> => {
    const res = await api.post<AuthResponse>('/auth/register-hospital', data);
    localStorage.setItem('lifelink_token', res.token);
    localStorage.setItem('lifelink_user', JSON.stringify(res.user));
    return res;
  },

  registerBloodBank: async (data: any): Promise<AuthResponse> => {
    const res = await api.post<AuthResponse>('/auth/register-blood-bank', data);
    localStorage.setItem('lifelink_token', res.token);
    localStorage.setItem('lifelink_user', JSON.stringify(res.user));
    return res;
  },

  getCurrentUser: async (): Promise<User> => {
    const user = await api.get<User>('/auth/me');
    localStorage.setItem('lifelink_user', JSON.stringify(user));
    return user;
  },

  logout: () => {
    localStorage.removeItem('lifelink_token');
    localStorage.removeItem('lifelink_user');
  },

  getStoredUser: (): User | null => {
    const raw = localStorage.getItem('lifelink_user');
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('lifelink_token');
  }
};
