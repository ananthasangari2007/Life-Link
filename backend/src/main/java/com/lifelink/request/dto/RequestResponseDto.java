package com.lifelink.request.dto;

import com.lifelink.request.model.BloodRequestResponse;

import java.time.Instant;

public record RequestResponseDto(
    Long id,
    Long bloodBankId,
    String bloodBankName,
    String status,
    int offeredUnits,
    String notes,
    Long respondedByUserId,
    String responderName,
    Instant respondedAt,
    Instant createdAt
) {
    public static RequestResponseDto fromEntity(BloodRequestResponse r) {
        if (r == null) return null;
        return new RequestResponseDto(
                r.getId(),
                r.getBloodBank().getId(),
                r.getBloodBank().getName(),
                r.getStatus().name(),
                r.getOfferedUnits(),
                r.getNotes(),
                r.getRespondedBy() != null ? r.getRespondedBy().getId() : null,
                r.getRespondedBy() != null ? r.getRespondedBy().getFullName() : null,
                r.getRespondedAt(),
                r.getCreatedAt()
        );
    }
}
