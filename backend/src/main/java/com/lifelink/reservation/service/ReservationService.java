package com.lifelink.reservation.service;

import com.lifelink.inventory.model.BloodInventoryBatch;
import com.lifelink.request.model.BloodRequest;
import com.lifelink.reservation.dto.ReservationDto;
import com.lifelink.reservation.model.StockReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service abstraction for atomic stock reservation and concurrency safety.
 * Demonstrates Abstraction and Transactional Concurrency Control.
 */
public interface ReservationService {

    StockReservation createReservation(BloodRequest request, BloodInventoryBatch batch, int units);

    ReservationDto releaseReservation(Long reservationId, String reason);

    List<ReservationDto> getRequestReservations(Long requestId);

    Page<ReservationDto> getBloodBankReservations(Long bloodBankId, Pageable pageable);

    void releaseExpiredReservations();
}
