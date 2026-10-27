package com.lifelink.matching.dto;

import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;

import java.util.List;

public record MatchResultDto(
    BloodGroup requestedGroup,
    String requestedGroupDisplay,
    ComponentType componentType,
    String componentTypeDisplay,
    int unitsRequested,
    int totalAvailableInNetwork,
    boolean fullMatchFound,
    List<CandidateFacilityMatchDto> candidateFacilities,
    List<FulfillmentPlanDto> suggestedPlans,
    String medicalNotice
) {}
