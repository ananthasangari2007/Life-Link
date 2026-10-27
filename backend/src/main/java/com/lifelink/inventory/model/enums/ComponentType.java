package com.lifelink.inventory.model.enums;

/**
 * Domain Enum representing Blood Components with standard shelf lives.
 */
public enum ComponentType {
    WHOLE_BLOOD("Whole Blood", 35),
    RED_BLOOD_CELLS("Packed Red Blood Cells (PRBC)", 42),
    PLATELETS("Platelet Concentrate", 5),
    FRESH_FROZEN_PLASMA("Fresh Frozen Plasma (FFP)", 365),
    CRYOPRECIPITATE("Cryoprecipitate", 365);

    private final String displayName;
    private final int standardShelfLifeDays;

    ComponentType(String displayName, int standardShelfLifeDays) {
        this.displayName = displayName;
        this.standardShelfLifeDays = standardShelfLifeDays;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getStandardShelfLifeDays() {
        return standardShelfLifeDays;
    }
}
