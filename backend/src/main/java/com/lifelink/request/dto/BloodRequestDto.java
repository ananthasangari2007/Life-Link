package com.lifelink.request.dto;

import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import com.lifelink.request.model.BloodRequest;
import com.lifelink.request.model.enums.RequestStatus;
import com.lifelink.request.model.enums.RequestUrgency;

import java.time.Instant;
import java.util.List;

public record BloodRequestDto(
    Long id,
    String requestReference,
    Long hospitalId,
    String hospitalName,
    Long requesterUserId,
    String requesterName,
    BloodGroup bloodGroup,
    String bloodGroupDisplay,
    ComponentType componentType,
    String componentTypeDisplay,
    int unitsRequested,
    int unitsFulfilled,
    RequestUrgency urgency,
    RequestStatus status,
    Instant requiredBy,
    String patientReferenceCode,
    String clinicalNotes,
    Instant createdAt,
    Instant updatedAt,
    List<RequestResponseDto> responses
) {
    public static BloodRequestDto fromEntity(BloodRequest r) {
        if (r == null) return null;
        List<RequestResponseDto> responses = r.getResponses() != null
                ? r.getResponses().stream().map(RequestResponseDto::fromEntity).toList()
                : List.of();

        return new BloodRequestDto(
                r.getId(),
                r.getRequestReference(),
                r.getHospital().getId(),
                r.getHospital().getName(),
                r.getRequester().getId(),
                r.getRequester().getFullName(),
                r.getBloodGroup(),
                r.getBloodGroup().getDisplayName(),
                r.getComponentType(),
                r.getComponentType().getDisplayName(),
                r.getUnitsRequested(),
                r.getUnitsFulfilled(),
                r.getUrgency(),
                r.getStatus(),
                r.getRequiredBy(),
                r.getPatientReferenceCode(),
                r.getClinicalNotes(),
                r.getCreatedAt(),
                r.getUpdatedAt(),
                responses
        );
    }
}
