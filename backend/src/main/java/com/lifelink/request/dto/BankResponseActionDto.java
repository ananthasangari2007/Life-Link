package com.lifelink.request.dto;

import jakarta.validation.constraints.NotBlank;

public record BankResponseActionDto(
    @NotBlank(message = "Action is required (ACCEPT, PARTIALLY_ACCEPT, REJECT)")
    String action,

    int offeredUnits,
    String notes
) {}
