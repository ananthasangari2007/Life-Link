package com.lifelink.request.dto;

import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import com.lifelink.request.model.enums.RequestUrgency;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record CreateBloodRequestDto(
    @NotNull(message = "Blood group is required")
    BloodGroup bloodGroup,

    @NotNull(message = "Component type is required")
    ComponentType componentType,

    @Min(value = 1, message = "Requested units must be at least 1")
    int unitsRequested,

    @NotNull(message = "Urgency is required")
    RequestUrgency urgency,

    @NotNull(message = "Required-by time is required")
    @Future(message = "Required-by time must be in the future")
    Instant requiredBy,

    String patientReferenceCode,
    String clinicalNotes,
    List<Long> targetBloodBankIds
) {}
