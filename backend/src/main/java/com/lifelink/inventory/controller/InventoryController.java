package com.lifelink.inventory.controller;

import com.lifelink.common.dto.ApiResponse;
import com.lifelink.common.dto.PageResponse;
import com.lifelink.inventory.dto.CreateBatchRequest;
import com.lifelink.inventory.dto.InventoryBatchDto;
import com.lifelink.inventory.dto.InventorySummaryDto;
import com.lifelink.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory", description = "Endpoints for blood inventory batch management")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/facilities/{facilityId}/batches")
    @PreAuthorize("hasAnyRole('BLOOD_BANK_USER', 'NETWORK_ADMIN')")
    @Operation(summary = "Add New Blood Batch", description = "Registers an audited intake batch into facility inventory")
    public ResponseEntity<ApiResponse<InventoryBatchDto>> addBatch(
            @PathVariable Long facilityId,
            @Valid @RequestBody CreateBatchRequest request
    ) {
        InventoryBatchDto dto = inventoryService.addBatch(facilityId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Batch registered successfully", dto));
    }

    @GetMapping("/facilities/{facilityId}/batches")
    @Operation(summary = "List Facility Batches")
    public ResponseEntity<ApiResponse<PageResponse<InventoryBatchDto>>> getBatches(
            @PathVariable Long facilityId,
            Pageable pageable
    ) {
        Page<InventoryBatchDto> page = inventoryService.getFacilityBatches(facilityId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements())));
    }

    @GetMapping("/facilities/{facilityId}/near-expiry")
    @Operation(summary = "Near-Expiry Stock Alerts", description = "Returns active batches expiring within the next 7 days")
    public ResponseEntity<ApiResponse<List<InventoryBatchDto>>> getNearExpiryBatches(@PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getNearExpiryBatches(facilityId)));
    }

    @GetMapping("/facilities/{facilityId}/summary")
    @Operation(summary = "Facility Stock Summary", description = "Aggregates available, reserved, and quarantined units")
    public ResponseEntity<ApiResponse<InventorySummaryDto>> getFacilitySummary(@PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getFacilitySummary(facilityId)));
    }

    @PatchMapping("/facilities/{facilityId}/batches/{batchId}/quarantine")
    @PreAuthorize("hasAnyRole('BLOOD_BANK_USER', 'NETWORK_ADMIN')")
    @Operation(summary = "Quarantine Batch", description = "Quarantines a batch from general availability")
    public ResponseEntity<ApiResponse<InventoryBatchDto>> quarantineBatch(
            @PathVariable Long facilityId,
            @PathVariable Long batchId,
            @RequestParam String reason
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Batch moved to quarantine", inventoryService.quarantineBatch(facilityId, batchId, reason)));
    }

    @PatchMapping("/facilities/{facilityId}/batches/{batchId}/restore")
    @PreAuthorize("hasAnyRole('BLOOD_BANK_USER', 'NETWORK_ADMIN')")
    @Operation(summary = "Restore Batch", description = "Restores a quarantined batch to available status")
    public ResponseEntity<ApiResponse<InventoryBatchDto>> restoreBatch(
            @PathVariable Long facilityId,
            @PathVariable Long batchId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Batch restored to active inventory", inventoryService.restoreBatch(facilityId, batchId)));
    }

    @GetMapping("/batches/{batchId}")
    @Operation(summary = "Get Batch Details")
    public ResponseEntity<ApiResponse<InventoryBatchDto>> getBatchById(@PathVariable Long batchId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getBatchById(batchId)));
    }
}
