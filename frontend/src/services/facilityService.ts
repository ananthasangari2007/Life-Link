import { api, type PageResponse } from './api';
import type { Facility, FacilityType, FacilityStatus } from '../types';

export const facilityService = {
  getPublicApproved: (type?: FacilityType) =>
    api.get<Facility[]>(`/facilities/public/approved${type ? `?type=${type}` : ''}`),

  getById: (id: number) => api.get<Facility>(`/facilities/${id}`),

  getMyFacility: () => api.get<Facility | null>('/facilities/my-facility'),

  getAllForAdmin: (status?: FacilityStatus, page = 0, size = 10) =>
    api.get<PageResponse<Facility>>(`/facilities/admin/all?page=${page}&size=${size}${status ? `&status=${status}` : ''}`),

  updateApproval: (id: number, approve: boolean, notes?: string) =>
    api.patch<Facility>(`/facilities/admin/${id}/approval`, { approve, notes }),

  suspend: (id: number, reason: string) =>
    api.patch<Facility>(`/facilities/admin/${id}/suspend?reason=${encodeURIComponent(reason)}`),
};
