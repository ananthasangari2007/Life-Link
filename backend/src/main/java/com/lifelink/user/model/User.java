package com.lifelink.user.model;

import com.lifelink.common.entity.BaseEntity;
import com.lifelink.facility.model.Facility;
import com.lifelink.user.model.enums.UserRole;
import jakarta.persistence.*;

/**
 * Domain entity representing an authenticated platform user.
 * Encapsulates credentials, roles, and facility affiliation.
 */
@Entity
@Table(name = "lifelink_users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Column(nullable = false)
    private boolean active = true;

    private String designation;

    public User() {}

    public User(String email, String passwordHash, String fullName, String phone,
                UserRole role, Facility facility, String designation) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.facility = facility;
        this.designation = designation;
        this.active = true;
    }

    public boolean isHospitalUser() {
        return this.role == UserRole.HOSPITAL_USER;
    }

    public boolean isBloodBankUser() {
        return this.role == UserRole.BLOOD_BANK_USER;
    }

    public boolean isNetworkAdmin() {
        return this.role == UserRole.NETWORK_ADMIN;
    }

    public boolean isAuditor() {
        return this.role == UserRole.AUDITOR;
    }

    public boolean belongsToFacility(Long targetFacilityId) {
        return this.facility != null && this.facility.getId().equals(targetFacilityId);
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}
