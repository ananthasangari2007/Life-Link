package com.lifelink.facility.dto;

import jakarta.validation.constraints.NotNull;

public record FacilityApprovalRequest(
    @NotNull(message = "Approve flag is required")
    Boolean approve,
    String notes
) {}
