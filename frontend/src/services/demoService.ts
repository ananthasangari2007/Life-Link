import { api } from './api';

export const demoService = {
  seedNetwork: () =>
    api.post<string>('/demo/seed'),

  getDemoAccounts: () =>
    api.get<Record<string, any>>('/demo/accounts'),
};
