package com.lifelink.inventory.model.enums;

/**
 * Domain Enum representing Blood Groups with helper methods for ABO and Rh factor logic.
 * Demonstrates OOP Enum behaviors.
 */
public enum BloodGroup {
    A_POSITIVE("A+", true, "A"),
    A_NEGATIVE("A-", false, "A"),
    B_POSITIVE("B+", true, "B"),
    B_NEGATIVE("B-", false, "B"),
    AB_POSITIVE("AB+", true, "AB"),
    AB_NEGATIVE("AB-", false, "AB"),
    O_POSITIVE("O+", true, "O"),
    O_NEGATIVE("O-", false, "O");

    private final String displayName;
    private final boolean rhPositive;
    private final String baseAbo;

    BloodGroup(String displayName, boolean rhPositive, String baseAbo) {
        this.displayName = displayName;
        this.rhPositive = rhPositive;
        this.baseAbo = baseAbo;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRhPositive() {
        return rhPositive;
    }

    public String getBaseAbo() {
        return baseAbo;
    }

    public static BloodGroup fromDisplayName(String name) {
        if (name == null) return null;
        for (BloodGroup bg : values()) {
            if (bg.displayName.equalsIgnoreCase(name.trim()) || bg.name().equalsIgnoreCase(name.trim())) {
                return bg;
            }
        }
        throw new IllegalArgumentException("Unknown blood group: " + name);
    }
}
