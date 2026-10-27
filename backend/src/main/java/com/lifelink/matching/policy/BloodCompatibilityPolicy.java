package com.lifelink.matching.policy;

import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;

import java.util.List;

/**
 * Abstraction for blood group compatibility policies.
 * Demonstrates the Strategy Pattern and Polymorphism.
 */
public interface BloodCompatibilityPolicy {

    /**
     * Returns the ordered list of acceptable donor blood groups for the given recipient.
     * Exact matches appear first, followed by compatible alternatives.
     */
    List<BloodGroup> getCompatibleDonorGroups(BloodGroup recipientGroup, ComponentType componentType);

    /**
     * Evaluates if a specific donor group is safe for the recipient group and component.
     */
    boolean isCompatible(BloodGroup recipientGroup, BloodGroup donorGroup, ComponentType componentType);

    /**
     * Returns a ranking preference score (higher is better, e.g. 100 for exact match, 80 for alternative).
     */
    int getCompatibilityScore(BloodGroup recipientGroup, BloodGroup donorGroup, ComponentType componentType);

    /**
     * Returns an explanation string for why this pairing is considered compatible or incompatible.
     */
    String getCompatibilityExplanation(BloodGroup recipientGroup, BloodGroup donorGroup, ComponentType componentType);
}
