package com.lifelink.analytics.dto;

import java.util.Map;

public record NetworkAnalyticsDto(
    int totalApprovedHospitals,
    int totalApprovedBloodBanks,
    int totalAvailableUnitsInNetwork,
    int totalReservedUnitsInNetwork,
    int totalRequestsCreated,
    int totalRequestsFulfilled,
    int activeTransfersCount,
    double averageFulfillmentRatePercent,
    Map<String, Integer> stockByBloodGroup,
    Map<String, Integer> stockByComponent
) {}
