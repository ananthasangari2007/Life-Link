package com.lifelink.reservation.controller;

import com.lifelink.common.dto.ApiResponse;
import com.lifelink.common.dto.PageResponse;
import com.lifelink.reservation.dto.ReservationDto;
import com.lifelink.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Stock Reservations", description = "Endpoints for managing locked stock reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/request/{requestId}")
    @Operation(summary = "Get Reservations by Request ID")
    public ResponseEntity<ApiResponse<List<ReservationDto>>> getRequestReservations(@PathVariable Long requestId) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.getRequestReservations(requestId)));
    }

    @GetMapping("/bank/{bloodBankId}")
    @PreAuthorize("hasAnyRole('BLOOD_BANK_USER', 'NETWORK_ADMIN')")
    @Operation(summary = "Get Reservations by Blood Bank")
    public ResponseEntity<ApiResponse<PageResponse<ReservationDto>>> getBankReservations(
            @PathVariable Long bloodBankId,
            Pageable pageable
    ) {
        Page<ReservationDto> page = reservationService.getBloodBankReservations(bloodBankId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements())));
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('BLOOD_BANK_USER', 'NETWORK_ADMIN')")
    @Operation(summary = "Release Reservation", description = "Releases reserved stock back to active inventory")
    public ResponseEntity<ApiResponse<ReservationDto>> releaseReservation(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Manual release") String reason
    ) {
        ReservationDto dto = reservationService.releaseReservation(id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Reservation released successfully", dto));
    }
}
