package com.lifelink.inventory.service;

import com.lifelink.inventory.dto.CreateBatchRequest;
import com.lifelink.inventory.dto.InventoryBatchDto;
import com.lifelink.inventory.dto.InventorySummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service abstraction for blood inventory lifecycle management.
 * Demonstrates Abstraction in the service layer.
 */
public interface InventoryService {

    InventoryBatchDto addBatch(Long facilityId, CreateBatchRequest request);

    Page<InventoryBatchDto> getFacilityBatches(Long facilityId, Pageable pageable);

    List<InventoryBatchDto> getNearExpiryBatches(Long facilityId);

    InventorySummaryDto getFacilitySummary(Long facilityId);

    InventoryBatchDto quarantineBatch(Long facilityId, Long batchId, String reason);

    InventoryBatchDto restoreBatch(Long facilityId, Long batchId);

    InventoryBatchDto getBatchById(Long batchId);
}
