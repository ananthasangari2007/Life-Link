package com.lifelink.transfer.model;

import com.lifelink.common.entity.BaseEntity;
import com.lifelink.facility.model.Facility;
import com.lifelink.request.model.BloodRequest;
import com.lifelink.reservation.model.StockReservation;
import com.lifelink.transfer.model.enums.TransferStatus;
import com.lifelink.user.model.User;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Domain entity representing an audited physical blood transfer between facilities.
 */
@Entity
@Table(name = "lifelink_blood_transfers")
public class BloodTransfer extends BaseEntity {

    @Column(name = "transfer_reference", nullable = false, unique = true)
    private String transferReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private BloodRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private StockReservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_facility_id", nullable = false)
    private Facility sourceFacility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_facility_id", nullable = false)
    private Facility destinationFacility;

    @Column(name = "units_transferred", nullable = false)
    private int unitsTransferred;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.PREPARED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatcher_user_id")
    private User dispatcher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id")
    private User receiver;

    @Column(name = "courier_details")
    private String courierDetails;

    @Column(name = "temperature_log_summary")
    private String temperatureLogSummary;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public BloodTransfer() {}

    public BloodTransfer(String transferReference, BloodRequest request, StockReservation reservation,
                         Facility sourceFacility, Facility destinationFacility, int unitsTransferred) {
        this.transferReference = transferReference;
        this.request = request;
        this.reservation = reservation;
        this.sourceFacility = sourceFacility;
        this.destinationFacility = destinationFacility;
        this.unitsTransferred = unitsTransferred;
        this.status = TransferStatus.PREPARED;
    }

    public void dispatch(User dispatcher, String courierDetails, String temperatureLogSummary, String notes) {
        if (this.status != TransferStatus.PREPARED) {
            throw new IllegalStateException("Only prepared transfers can be dispatched.");
        }
        this.dispatcher = dispatcher;
        this.courierDetails = courierDetails;
        this.temperatureLogSummary = temperatureLogSummary;
        this.notes = notes;
        this.dispatchedAt = Instant.now();
        this.status = TransferStatus.IN_TRANSIT;
    }

    public void confirmReceipt(User receiver, String notes) {
        if (this.status != TransferStatus.IN_TRANSIT && this.status != TransferStatus.DISPATCHED) {
            throw new IllegalStateException("Transfer must be in transit before receipt can be confirmed.");
        }
        this.receiver = receiver;
        this.receivedAt = Instant.now();
        this.status = TransferStatus.RECEIVED;
        if (notes != null && !notes.isBlank()) {
            this.notes = (this.notes == null ? "" : this.notes + "; ") + "Receipt: " + notes;
        }
    }

    public void cancel(String reason) {
        if (this.status == TransferStatus.RECEIVED) {
            throw new IllegalStateException("Cannot cancel an already received transfer.");
        }
        this.status = TransferStatus.CANCELLED;
        this.notes = (this.notes == null ? "" : this.notes + "; ") + "Cancelled: " + reason;
    }

    public String getTransferReference() {
        return transferReference;
    }

    public BloodRequest getRequest() {
        return request;
    }

    public StockReservation getReservation() {
        return reservation;
    }

    public Facility getSourceFacility() {
        return sourceFacility;
    }

    public Facility getDestinationFacility() {
        return destinationFacility;
    }

    public int getUnitsTransferred() {
        return unitsTransferred;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public User getDispatcher() {
        return dispatcher;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getCourierDetails() {
        return courierDetails;
    }

    public String getTemperatureLogSummary() {
        return temperatureLogSummary;
    }

    public Instant getDispatchedAt() {
        return dispatchedAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public String getNotes() {
        return notes;
    }
}
