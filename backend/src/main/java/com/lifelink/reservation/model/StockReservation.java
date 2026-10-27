package com.lifelink.reservation.model;

import com.lifelink.common.entity.BaseEntity;
import com.lifelink.facility.model.Facility;
import com.lifelink.inventory.model.BloodInventoryBatch;
import com.lifelink.request.model.BloodRequest;
import com.lifelink.reservation.model.enums.ReservationStatus;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Domain entity representing an active hold on an inventory batch.
 * Prevents double-allocation across concurrent hospital requests.
 */
@Entity
@Table(name = "lifelink_stock_reservations")
public class StockReservation extends BaseEntity {

    @Column(name = "reservation_reference", nullable = false, unique = true)
    private String reservationReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private BloodRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private BloodInventoryBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bank_facility_id", nullable = false)
    private Facility bloodBank;

    @Column(name = "units_reserved", nullable = false)
    private int unitsReserved;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public StockReservation() {}

    public StockReservation(String reservationReference, BloodRequest request,
                            BloodInventoryBatch batch, Facility bloodBank,
                            int unitsReserved, Instant expiresAt) {
        if (unitsReserved <= 0) {
            throw new IllegalArgumentException("Units reserved must be positive.");
        }
        this.reservationReference = reservationReference;
        this.request = request;
        this.batch = batch;
        this.bloodBank = bloodBank;
        this.unitsReserved = unitsReserved;
        this.status = ReservationStatus.ACTIVE;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return this.status == ReservationStatus.ACTIVE && Instant.now().isAfter(this.expiresAt);
    }

    public void confirm() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Only active reservations can be confirmed.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void release() {
        if (this.status == ReservationStatus.DISPATCHED) {
            throw new IllegalStateException("Cannot release an already dispatched reservation.");
        }
        this.status = ReservationStatus.RELEASED;
    }

    public void markDispatched() {
        if (this.status != ReservationStatus.ACTIVE && this.status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only active or confirmed reservations can be dispatched.");
        }
        this.status = ReservationStatus.DISPATCHED;
    }

    public void markExpired() {
        this.status = ReservationStatus.EXPIRED;
    }

    public String getReservationReference() {
        return reservationReference;
    }

    public BloodRequest getRequest() {
        return request;
    }

    public BloodInventoryBatch getBatch() {
        return batch;
    }

    public Facility getBloodBank() {
        return bloodBank;
    }

    public int getUnitsReserved() {
        return unitsReserved;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
