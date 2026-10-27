import { api, type PageResponse } from './api';
import type { BloodTransfer } from '../types';

export const transferService = {
  dispatchTransfer: (reservationId: number, courierDetails?: string, temperatureLogSummary?: string, notes?: string) =>
    api.post<BloodTransfer>('/transfers/dispatch', { reservationId, courierDetails, temperatureLogSummary, notes }),

  confirmReceipt: (transferId: number, notes?: string) =>
    api.post<BloodTransfer>(`/transfers/${transferId}/confirm-receipt`, { notes }),

  getById: (transferId: number) =>
    api.get<BloodTransfer>(`/transfers/${transferId}`),

  getSourceTransfers: (facilityId: number, page = 0, size = 10) =>
    api.get<PageResponse<BloodTransfer>>(`/transfers/source/${facilityId}?page=${page}&size=${size}`),

  getDestinationTransfers: (facilityId: number, page = 0, size = 10) =>
    api.get<PageResponse<BloodTransfer>>(`/transfers/destination/${facilityId}?page=${page}&size=${size}`),
};
