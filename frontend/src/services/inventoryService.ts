import { api, type PageResponse } from './api';
import type { BloodInventoryBatch, InventorySummary } from '../types';

export const inventoryService = {
  addBatch: (facilityId: number, data: any) =>
    api.post<BloodInventoryBatch>(`/inventory/facilities/${facilityId}/batches`, data),

  getBatches: (facilityId: number, page = 0, size = 15) =>
    api.get<PageResponse<BloodInventoryBatch>>(`/inventory/facilities/${facilityId}/batches?page=${page}&size=${size}`),

  getNearExpiry: (facilityId: number) =>
    api.get<BloodInventoryBatch[]>(`/inventory/facilities/${facilityId}/near-expiry`),

  getSummary: (facilityId: number) =>
    api.get<InventorySummary>(`/inventory/facilities/${facilityId}/summary`),

  quarantineBatch: (facilityId: number, batchId: number, reason: string) =>
    api.patch<BloodInventoryBatch>(`/inventory/facilities/${facilityId}/batches/${batchId}/quarantine?reason=${encodeURIComponent(reason)}`),

  restoreBatch: (facilityId: number, batchId: number) =>
    api.patch<BloodInventoryBatch>(`/inventory/facilities/${facilityId}/batches/${batchId}/restore`),

  getBatchById: (batchId: number) =>
    api.get<BloodInventoryBatch>(`/inventory/batches/${batchId}`),
};
