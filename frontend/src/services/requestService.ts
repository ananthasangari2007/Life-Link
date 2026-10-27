import { api, type PageResponse } from './api';
import type { BloodRequest, RequestStatus } from '../types';

export const requestService = {
  createRequest: (data: any) =>
    api.post<BloodRequest>('/requests', data),

  getById: (id: number) =>
    api.get<BloodRequest>(`/requests/${id}`),

  getByReference: (ref: string) =>
    api.get<BloodRequest>(`/requests/reference/${ref}`),

  getHospitalRequests: (hospitalId: number, page = 0, size = 10) =>
    api.get<PageResponse<BloodRequest>>(`/requests/hospital/${hospitalId}?page=${page}&size=${size}`),

  getBankIncomingRequests: (bloodBankId: number, page = 0, size = 10) =>
    api.get<PageResponse<BloodRequest>>(`/requests/incoming/${bloodBankId}?page=${page}&size=${size}`),

  getAllRequests: (status?: RequestStatus, page = 0, size = 10) =>
    api.get<PageResponse<BloodRequest>>(`/requests/admin/all?page=${page}&size=${size}${status ? `&status=${status}` : ''}`),

  respondToRequest: (requestId: number, bloodBankId: number, action: 'ACCEPT' | 'PARTIALLY_ACCEPT' | 'REJECT', offeredUnits: number, notes?: string) =>
    api.post<BloodRequest>(`/requests/${requestId}/respond/${bloodBankId}`, { action, offeredUnits, notes }),

  cancelRequest: (requestId: number, reason?: string) =>
    api.post<BloodRequest>(`/requests/${requestId}/cancel?reason=${encodeURIComponent(reason || 'Cancelled by requester')}`),
};
