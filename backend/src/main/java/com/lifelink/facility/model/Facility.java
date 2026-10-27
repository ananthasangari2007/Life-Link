package com.lifelink.facility.model;

import com.lifelink.common.entity.BaseEntity;
import com.lifelink.facility.model.enums.FacilityStatus;
import com.lifelink.facility.model.enums.FacilityType;
import jakarta.persistence.*;

/**
 * Domain entity representing a participating Hospital or Blood Bank.
 * Demonstrates Encapsulation with controlled state transitions.
 */
@Entity
@Table(name = "lifelink_facilities")
public class Facility extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FacilityType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FacilityStatus status = FacilityStatus.PENDING;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String pincode;

    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    private Double latitude;
    private Double longitude;

    @Column(name = "operating_hours")
    private String operatingHours;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;

    public Facility() {}

    public Facility(String name, String licenseNumber, FacilityType type, String address,
                    String city, String state, String pincode, String contactPhone, String contactEmail) {
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.type = type;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.status = FacilityStatus.PENDING;
    }

    // Controlled business transitions
    public void approve(String notes) {
        this.status = FacilityStatus.APPROVED;
        this.approvalNotes = notes;
    }

    public void suspend(String reason) {
        this.status = FacilityStatus.SUSPENDED;
        this.approvalNotes = reason;
    }

    public void reject(String reason) {
        this.status = FacilityStatus.REJECTED;
        this.approvalNotes = reason;
    }

    public boolean isApproved() {
        return this.status == FacilityStatus.APPROVED;
    }

    // Getters and controlled Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public FacilityType getType() {
        return type;
    }

    public void setType(FacilityType type) {
        this.type = type;
    }

    public FacilityStatus getStatus() {
        return status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getApprovalNotes() {
        return approvalNotes;
    }
}
