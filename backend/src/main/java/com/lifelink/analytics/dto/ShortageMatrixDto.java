package com.lifelink.analytics.dto;

import java.util.List;

public record ShortageMatrixDto(
    String bloodGroup,
    String componentType,
    int availableUnits,
    int minimumSafetyThreshold,
    String shortageSeverity // "NORMAL", "LOW", "CRITICAL"
) {}
