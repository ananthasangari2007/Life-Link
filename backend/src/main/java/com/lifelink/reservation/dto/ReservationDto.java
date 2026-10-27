package com.lifelink.reservation.dto;

import com.lifelink.inventory.dto.InventoryBatchDto;
import com.lifelink.reservation.model.StockReservation;
import com.lifelink.reservation.model.enums.ReservationStatus;

import java.time.Instant;

public record ReservationDto(
    Long id,
    String reservationReference,
    Long requestId,
    String requestReference,
    Long bloodBankId,
    String bloodBankName,
    Long batchId,
    String batchNumber,
    String bloodGroupDisplay,
    String componentTypeDisplay,
    int unitsReserved,
    ReservationStatus status,
    Instant expiresAt,
    boolean expired,
    Instant createdAt
) {
    public static ReservationDto fromEntity(StockReservation sr) {
        if (sr == null) return null;
        return new ReservationDto(
                sr.getId(),
                sr.getReservationReference(),
                sr.getRequest().getId(),
                sr.getRequest().getRequestReference(),
                sr.getBloodBank().getId(),
                sr.getBloodBank().getName(),
                sr.getBatch().getId(),
                sr.getBatch().getBatchNumber(),
                sr.getBatch().getBloodGroup().getDisplayName(),
                sr.getBatch().getComponentType().getDisplayName(),
                sr.getUnitsReserved(),
                sr.getStatus(),
                sr.getExpiresAt(),
                sr.isExpired(),
                sr.getCreatedAt()
        );
    }
}
