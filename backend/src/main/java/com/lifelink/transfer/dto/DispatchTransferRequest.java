package com.lifelink.transfer.dto;

import jakarta.validation.constraints.NotNull;

public record DispatchTransferRequest(
    @NotNull(message = "Reservation ID is required")
    Long reservationId,
    String courierDetails,
    String temperatureLogSummary,
    String notes
) {}
