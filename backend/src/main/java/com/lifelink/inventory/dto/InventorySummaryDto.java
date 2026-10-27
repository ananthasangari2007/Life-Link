package com.lifelink.inventory.dto;

import java.util.Map;

public record InventorySummaryDto(
    int totalUnits,
    int availableUnits,
    int reservedUnits,
    int dispatchedUnits,
    int nearExpiryUnits,
    int quarantinedUnits,
    Map<String, Integer> stockByBloodGroup,
    Map<String, Integer> stockByComponent
) {}
