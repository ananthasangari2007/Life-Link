import { api, type PageResponse } from './api';
import type { NetworkAnalytics, ShortageMatrixItem, AuditLog } from '../types';

export const analyticsService = {
  getNetworkAnalytics: () =>
    api.get<NetworkAnalytics>('/analytics/network'),

  getShortageMatrix: () =>
    api.get<ShortageMatrixItem[]>('/analytics/shortages'),

  getAuditLogs: (facilityId?: number, page = 0, size = 15) =>
    api.get<PageResponse<AuditLog>>(`/audit?page=${page}&size=${size}${facilityId ? `&facilityId=${facilityId}` : ''}`),
};
