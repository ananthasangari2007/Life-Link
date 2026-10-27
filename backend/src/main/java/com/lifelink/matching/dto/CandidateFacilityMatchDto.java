package com.lifelink.matching.dto;

import com.lifelink.inventory.dto.InventoryBatchDto;
import com.lifelink.inventory.model.enums.BloodGroup;

import java.util.List;

public record CandidateFacilityMatchDto(
    Long facilityId,
    String facilityName,
    String address,
    String city,
    String contactPhone,
    String contactEmail,
    BloodGroup bloodGroup,
    String bloodGroupDisplay,
    int availableUnits,
    int compatibilityScore,
    String compatibilityExplanation,
    boolean canFulfillEntirely,
    List<InventoryBatchDto> availableBatches
) {}
