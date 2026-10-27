package com.lifelink.user.model.enums;

public enum UserRole {
    HOSPITAL_USER,
    BLOOD_BANK_USER,
    NETWORK_ADMIN,
    AUDITOR;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
