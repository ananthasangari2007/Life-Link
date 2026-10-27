package com.lifelink.inventory.dto;

import com.lifelink.inventory.model.BloodInventoryBatch;
import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import com.lifelink.inventory.model.enums.InventoryStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record InventoryBatchDto(
    Long id,
    Long facilityId,
    String facilityName,
    String batchNumber,
    BloodGroup bloodGroup,
    String bloodGroupDisplay,
    ComponentType componentType,
    String componentTypeDisplay,
    int quantity,
    int reservedQuantity,
    int dispatchedQuantity,
    int availableQuantity,
    Instant collectionDate,
    Instant expiryDate,
    long daysUntilExpiry,
    InventoryStatus status,
    String storageLocation,
    String notes,
    boolean nearExpiry,
    boolean expired
) {
    public static InventoryBatchDto fromEntity(BloodInventoryBatch b) {
        if (b == null) return null;
        Instant now = Instant.now();
        long daysUntilExpiry = ChronoUnit.DAYS.between(now, b.getExpiryDate());
        boolean expired = b.isExpired();
        boolean nearExpiry = !expired && daysUntilExpiry <= 7;

        return new InventoryBatchDto(
                b.getId(),
                b.getFacility().getId(),
                b.getFacility().getName(),
                b.getBatchNumber(),
                b.getBloodGroup(),
                b.getBloodGroup().getDisplayName(),
                b.getComponentType(),
                b.getComponentType().getDisplayName(),
                b.getQuantity(),
                b.getReservedQuantity(),
                b.getDispatchedQuantity(),
                b.getAvailableQuantity(),
                b.getCollectionDate(),
                b.getExpiryDate(),
                daysUntilExpiry,
                b.getStatus(),
                b.getStorageLocation(),
                b.getNotes(),
                nearExpiry,
                expired
        );
    }
}
