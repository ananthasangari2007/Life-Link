import { api, type PageResponse } from './api';
import type { StockReservation } from '../types';

export const reservationService = {
  getRequestReservations: (requestId: number) =>
    api.get<StockReservation[]>(`/reservations/request/${requestId}`),

  getBankReservations: (bloodBankId: number, page = 0, size = 15) =>
    api.get<PageResponse<StockReservation>>(`/reservations/bank/${bloodBankId}?page=${page}&size=${size}`),

  releaseReservation: (id: number, reason?: string) =>
    api.post<StockReservation>(`/reservations/${id}/release?reason=${encodeURIComponent(reason || 'Manual release')}`),
};
