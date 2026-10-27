package com.lifelink.matching.dto;

import com.lifelink.inventory.model.enums.BloodGroup;

public record PlanSegmentDto(
    Long facilityId,
    String facilityName,
    String city,
    BloodGroup bloodGroup,
    String bloodGroupDisplay,
    int unitsAllocated,
    String reason
) {}
