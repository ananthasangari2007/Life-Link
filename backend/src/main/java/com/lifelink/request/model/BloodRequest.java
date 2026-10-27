package com.lifelink.request.model;

import com.lifelink.common.entity.BaseEntity;
import com.lifelink.facility.model.Facility;
import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import com.lifelink.request.model.enums.RequestStatus;
import com.lifelink.request.model.enums.RequestUrgency;
import com.lifelink.user.model.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a hospital's blood request.
 * Demonstrates Composition (contains responses) and Encapsulation of the lifecycle state machine.
 */
@Entity
@Table(name = "lifelink_blood_requests")
public class BloodRequest extends BaseEntity {

    @Column(name = "request_reference", nullable = false, unique = true)
    private String requestReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_facility_id", nullable = false)
    private Facility hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    private BloodGroup bloodGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    private ComponentType componentType;

    @Column(name = "units_requested", nullable = false)
    private int unitsRequested;

    @Column(name = "units_fulfilled", nullable = false)
    private int unitsFulfilled = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestUrgency urgency = RequestUrgency.ROUTINE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.SUBMITTED;

    @Column(name = "required_by", nullable = false)
    private Instant requiredBy;

    @Column(name = "patient_reference_code")
    private String patientReferenceCode;

    @Column(name = "clinical_notes", columnDefinition = "TEXT")
    private String clinicalNotes;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BloodRequestResponse> responses = new ArrayList<>();

    public BloodRequest() {}

    public BloodRequest(String requestReference, Facility hospital, User requester,
                        BloodGroup bloodGroup, ComponentType componentType, int unitsRequested,
                        RequestUrgency urgency, Instant requiredBy, String patientReferenceCode,
                        String clinicalNotes) {
        if (unitsRequested <= 0) {
            throw new IllegalArgumentException("Requested units must be greater than zero.");
        }
        this.requestReference = requestReference;
        this.hospital = hospital;
        this.requester = requester;
        this.bloodGroup = bloodGroup;
        this.componentType = componentType;
        this.unitsRequested = unitsRequested;
        this.unitsFulfilled = 0;
        this.urgency = urgency != null ? urgency : RequestUrgency.ROUTINE;
        this.status = RequestStatus.SUBMITTED;
        this.requiredBy = requiredBy;
        this.patientReferenceCode = patientReferenceCode;
        this.clinicalNotes = clinicalNotes;
    }

    public void addResponse(BloodRequestResponse response) {
        this.responses.add(response);
        response.setRequest(this);
    }

    public void transitionStatus(RequestStatus nextStatus) {
        this.status = nextStatus;
    }

    public void recordFulfillment(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("Fulfilled units must be positive.");
        }
        this.unitsFulfilled += units;
        if (this.unitsFulfilled >= this.unitsRequested) {
            this.status = RequestStatus.ACCEPTED;
        } else {
            this.status = RequestStatus.PARTIALLY_FULFILLED;
        }
    }

    // Getters and Setters
    public String getRequestReference() {
        return requestReference;
    }

    public Facility getHospital() {
        return hospital;
    }

    public User getRequester() {
        return requester;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public int getUnitsRequested() {
        return unitsRequested;
    }

    public int getUnitsFulfilled() {
        return unitsFulfilled;
    }

    public RequestUrgency getUrgency() {
        return urgency;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public Instant getRequiredBy() {
        return requiredBy;
    }

    public String getPatientReferenceCode() {
        return patientReferenceCode;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public List<BloodRequestResponse> getResponses() {
        return responses;
    }
}
