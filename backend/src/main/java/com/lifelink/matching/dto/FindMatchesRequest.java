package com.lifelink.matching.dto;

import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import com.lifelink.request.model.enums.RequestUrgency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record FindMatchesRequest(
    @NotNull(message = "Blood group is required")
    BloodGroup bloodGroup,

    @NotNull(message = "Component type is required")
    ComponentType componentType,

    @Min(value = 1, message = "Requested units must be at least 1")
    int unitsRequested,

    String hospitalLocation,
    Instant requiredBy,
    RequestUrgency urgency
) {}
