package com.lifelink.matching.dto;

import java.util.List;

public record FulfillmentPlanDto(
    String planType, // "SINGLE_BANK" or "MULTI_BANK_SPLIT"
    String title,
    String summary,
    int totalUnitsProposed,
    boolean completesRequest,
    List<PlanSegmentDto> segments
) {}
