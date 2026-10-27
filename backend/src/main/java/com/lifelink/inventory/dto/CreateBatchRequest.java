package com.lifelink.inventory.dto;

import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateBatchRequest(
    @NotBlank(message = "Batch number is required")
    String batchNumber,

    @NotNull(message = "Blood group is required")
    BloodGroup bloodGroup,

    @NotNull(message = "Component type is required")
    ComponentType componentType,

    @Min(value = 1, message = "Quantity must be at least 1 unit")
    int quantity,

    Instant collectionDate,

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    Instant expiryDate,

    String storageLocation,
    String notes
) {}
