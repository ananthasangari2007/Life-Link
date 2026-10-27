package com.lifelink.request.model;

import com.lifelink.facility.model.Facility;
import com.lifelink.request.model.enums.BankResponseStatus;
import com.lifelink.user.model.User;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Domain entity representing an individual blood bank's response to a multi-bank request.
 */
@Entity
@Table(name = "lifelink_blood_request_responses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"request_id", "blood_bank_facility_id"}))
public class BloodRequestResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private BloodRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bank_facility_id", nullable = false)
    private Facility bloodBank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BankResponseStatus status = BankResponseStatus.PENDING;

    @Column(name = "offered_units", nullable = false)
    private int offeredUnits = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by_user_id")
    private User respondedBy;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public BloodRequestResponse() {}

    public BloodRequestResponse(BloodRequest request, Facility bloodBank) {
        this.request = request;
        this.bloodBank = bloodBank;
        this.status = BankResponseStatus.PENDING;
        this.offeredUnits = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void accept(int units, User responder, String notes) {
        this.status = BankResponseStatus.ACCEPTED;
        this.offeredUnits = units;
        this.respondedBy = responder;
        this.respondedAt = Instant.now();
        this.updatedAt = Instant.now();
        this.notes = notes;
    }

    public void partiallyAccept(int units, User responder, String notes) {
        this.status = BankResponseStatus.PARTIALLY_ACCEPTED;
        this.offeredUnits = units;
        this.respondedBy = responder;
        this.respondedAt = Instant.now();
        this.updatedAt = Instant.now();
        this.notes = notes;
    }

    public void reject(User responder, String notes) {
        this.status = BankResponseStatus.REJECTED;
        this.offeredUnits = 0;
        this.respondedBy = responder;
        this.respondedAt = Instant.now();
        this.updatedAt = Instant.now();
        this.notes = notes;
    }

    public void withdraw() {
        this.status = BankResponseStatus.WITHDRAWN;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public BloodRequest getRequest() {
        return request;
    }

    public void setRequest(BloodRequest request) {
        this.request = request;
    }

    public Facility getBloodBank() {
        return bloodBank;
    }

    public BankResponseStatus getStatus() {
        return status;
    }

    public int getOfferedUnits() {
        return offeredUnits;
    }

    public String getNotes() {
        return notes;
    }

    public User getRespondedBy() {
        return respondedBy;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
