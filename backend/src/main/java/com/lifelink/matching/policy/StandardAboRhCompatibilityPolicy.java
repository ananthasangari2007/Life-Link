package com.lifelink.matching.policy;

import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard medical ABO and Rh factor compatibility engine.
 * Implements RBC, Plasma, Platelet, and Whole Blood compatibility rules.
 * Demonstrates Polymorphism and Strategy implementation.
 */
@Component
public class StandardAboRhCompatibilityPolicy implements BloodCompatibilityPolicy {

    @Override
    public List<BloodGroup> getCompatibleDonorGroups(BloodGroup recipient, ComponentType component) {
        if (recipient == null || component == null) {
            return List.of();
        }

        List<BloodGroup> compatible = new ArrayList<>();
        // Exact match is always the highest priority
        compatible.add(recipient);

        switch (component) {
            case RED_BLOOD_CELLS -> {
                // Red Blood Cells: O- is universal donor
                switch (recipient) {
                    case A_POSITIVE -> addIfMissing(compatible, BloodGroup.A_NEGATIVE, BloodGroup.O_POSITIVE, BloodGroup.O_NEGATIVE);
                    case A_NEGATIVE -> addIfMissing(compatible, BloodGroup.O_NEGATIVE);
                    case B_POSITIVE -> addIfMissing(compatible, BloodGroup.B_NEGATIVE, BloodGroup.O_POSITIVE, BloodGroup.O_NEGATIVE);
                    case B_NEGATIVE -> addIfMissing(compatible, BloodGroup.O_NEGATIVE);
                    case AB_POSITIVE -> addIfMissing(compatible, BloodGroup.AB_NEGATIVE, BloodGroup.A_POSITIVE, BloodGroup.A_NEGATIVE,
                            BloodGroup.B_POSITIVE, BloodGroup.B_NEGATIVE, BloodGroup.O_POSITIVE, BloodGroup.O_NEGATIVE);
                    case AB_NEGATIVE -> addIfMissing(compatible, BloodGroup.A_NEGATIVE, BloodGroup.B_NEGATIVE, BloodGroup.O_NEGATIVE);
                    case O_POSITIVE -> addIfMissing(compatible, BloodGroup.O_NEGATIVE);
                    case O_NEGATIVE -> { /* Only O- can donate to O- */ }
                }
            }
            case FRESH_FROZEN_PLASMA, CRYOPRECIPITATE -> {
                // Plasma contains antibodies: AB is universal donor, O is universal recipient (inverse of RBC)
                switch (recipient) {
                    case O_POSITIVE -> addIfMissing(compatible, BloodGroup.O_NEGATIVE, BloodGroup.A_POSITIVE, BloodGroup.A_NEGATIVE,
                            BloodGroup.B_POSITIVE, BloodGroup.B_NEGATIVE, BloodGroup.AB_POSITIVE, BloodGroup.AB_NEGATIVE);
                    case O_NEGATIVE -> addIfMissing(compatible, BloodGroup.O_POSITIVE, BloodGroup.A_NEGATIVE, BloodGroup.B_NEGATIVE,
                            BloodGroup.AB_NEGATIVE, BloodGroup.A_POSITIVE, BloodGroup.B_POSITIVE, BloodGroup.AB_POSITIVE);
                    case A_POSITIVE, A_NEGATIVE -> addIfMissing(compatible, BloodGroup.A_POSITIVE, BloodGroup.A_NEGATIVE,
                            BloodGroup.AB_POSITIVE, BloodGroup.AB_NEGATIVE);
                    case B_POSITIVE, B_NEGATIVE -> addIfMissing(compatible, BloodGroup.B_POSITIVE, BloodGroup.B_NEGATIVE,
                            BloodGroup.AB_POSITIVE, BloodGroup.AB_NEGATIVE);
                    case AB_POSITIVE, AB_NEGATIVE -> addIfMissing(compatible, BloodGroup.AB_POSITIVE, BloodGroup.AB_NEGATIVE);
                }
            }
            case PLATELETS -> {
                // Platelets prefer ABO identical, then compatible plasma/cell rules
                switch (recipient) {
                    case A_POSITIVE -> addIfMissing(compatible, BloodGroup.A_NEGATIVE, BloodGroup.AB_POSITIVE, BloodGroup.O_POSITIVE);
                    case A_NEGATIVE -> addIfMissing(compatible, BloodGroup.A_POSITIVE, BloodGroup.O_NEGATIVE);
                    case B_POSITIVE -> addIfMissing(compatible, BloodGroup.B_NEGATIVE, BloodGroup.AB_POSITIVE, BloodGroup.O_POSITIVE);
                    case B_NEGATIVE -> addIfMissing(compatible, BloodGroup.B_POSITIVE, BloodGroup.O_NEGATIVE);
                    case AB_POSITIVE -> addIfMissing(compatible, BloodGroup.AB_NEGATIVE, BloodGroup.A_POSITIVE, BloodGroup.B_POSITIVE);
                    case AB_NEGATIVE -> addIfMissing(compatible, BloodGroup.AB_POSITIVE, BloodGroup.A_NEGATIVE, BloodGroup.B_NEGATIVE);
                    case O_POSITIVE -> addIfMissing(compatible, BloodGroup.O_NEGATIVE, BloodGroup.A_POSITIVE);
                    case O_NEGATIVE -> addIfMissing(compatible, BloodGroup.O_POSITIVE);
                }
            }
            case WHOLE_BLOOD -> {
                // Whole Blood generally requires identical ABO and Rh for safety
                // Only O- can occasionally serve as emergency backup for Rh negative
                if (!recipient.isRhPositive()) {
                    addIfMissing(compatible, BloodGroup.O_NEGATIVE);
                }
            }
        }
        return compatible;
    }

    private void addIfMissing(List<BloodGroup> list, BloodGroup... groups) {
        for (BloodGroup g : groups) {
            if (!list.contains(g)) {
                list.add(g);
            }
        }
    }

    @Override
    public boolean isCompatible(BloodGroup recipient, BloodGroup donor, ComponentType component) {
        return getCompatibleDonorGroups(recipient, component).contains(donor);
    }

    @Override
    public int getCompatibilityScore(BloodGroup recipient, BloodGroup donor, ComponentType component) {
        if (recipient == donor) {
            return 100; // Perfect match
        }
        List<BloodGroup> compatible = getCompatibleDonorGroups(recipient, component);
        int index = compatible.indexOf(donor);
        if (index < 0) {
            return 0; // Incompatible
        }
        // Score degrades smoothly based on priority ranking
        return Math.max(50, 95 - (index * 10));
    }

    @Override
    public String getCompatibilityExplanation(BloodGroup recipient, BloodGroup donor, ComponentType component) {
        if (recipient == donor) {
            return "Exact ABO and Rh match (" + recipient.getDisplayName() + ")";
        }
        if (!isCompatible(recipient, donor, component)) {
            return "Incompatible: donor " + donor.getDisplayName() + " cannot safely be given to recipient " + recipient.getDisplayName() + " for " + component.getDisplayName();
        }
        if (component == ComponentType.RED_BLOOD_CELLS && donor == BloodGroup.O_NEGATIVE) {
            return "Compatible universal donor (O-) for recipient " + recipient.getDisplayName();
        }
        if ((component == ComponentType.FRESH_FROZEN_PLASMA || component == ComponentType.CRYOPRECIPITATE)
                && (donor == BloodGroup.AB_POSITIVE || donor == BloodGroup.AB_NEGATIVE)) {
            return "Compatible universal plasma donor (AB) for recipient " + recipient.getDisplayName();
        }
        return "Acceptable compatible alternative: " + donor.getDisplayName() + " for recipient " + recipient.getDisplayName();
    }
}
