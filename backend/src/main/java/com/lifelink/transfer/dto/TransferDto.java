package com.lifelink.transfer.dto;

import com.lifelink.transfer.model.BloodTransfer;
import com.lifelink.transfer.model.enums.TransferStatus;

import java.time.Instant;

public record TransferDto(
    Long id,
    String transferReference,
    Long requestId,
    String requestReference,
    Long reservationId,
    Long sourceFacilityId,
    String sourceFacilityName,
    Long destinationFacilityId,
    String destinationFacilityName,
    int unitsTransferred,
    TransferStatus status,
    Long dispatcherUserId,
    String dispatcherName,
    Long receiverUserId,
    String receiverName,
    String courierDetails,
    String temperatureLogSummary,
    Instant dispatchedAt,
    Instant receivedAt,
    String notes,
    Instant createdAt
) {
    public static TransferDto fromEntity(BloodTransfer t) {
        if (t == null) return null;
        return new TransferDto(
                t.getId(),
                t.getTransferReference(),
                t.getRequest().getId(),
                t.getRequest().getRequestReference(),
                t.getReservation().getId(),
                t.getSourceFacility().getId(),
                t.getSourceFacility().getName(),
                t.getDestinationFacility().getId(),
                t.getDestinationFacility().getName(),
                t.getUnitsTransferred(),
                t.getStatus(),
                t.getDispatcher() != null ? t.getDispatcher().getId() : null,
                t.getDispatcher() != null ? t.getDispatcher().getFullName() : null,
                t.getReceiver() != null ? t.getReceiver().getId() : null,
                t.getReceiver() != null ? t.getReceiver().getFullName() : null,
                t.getCourierDetails(),
                t.getTemperatureLogSummary(),
                t.getDispatchedAt(),
                t.getReceivedAt(),
                t.getNotes(),
                t.getCreatedAt()
        );
    }
}
