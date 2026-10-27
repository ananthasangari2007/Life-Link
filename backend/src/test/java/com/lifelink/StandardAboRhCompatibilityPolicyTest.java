package com.lifelink;

import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import com.lifelink.matching.policy.StandardAboRhCompatibilityPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Blood Group Compatibility Policy Unit Tests")
class StandardAboRhCompatibilityPolicyTest {

    private StandardAboRhCompatibilityPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new StandardAboRhCompatibilityPolicy();
    }

    @Test
    @DisplayName("RBC: O- is universal red blood cell donor")
    void testUniversalRbcDonor() {
        for (BloodGroup recipient : BloodGroup.values()) {
            assertTrue(policy.isCompatible(recipient, BloodGroup.O_NEGATIVE, ComponentType.RED_BLOOD_CELLS),
                    "O- should be compatible for all recipients for RBC, failed for " + recipient);
        }
    }

    @Test
    @DisplayName("RBC: AB+ can receive from any blood group (Universal Recipient)")
    void testUniversalRbcRecipient() {
        for (BloodGroup donor : BloodGroup.values()) {
            assertTrue(policy.isCompatible(BloodGroup.AB_POSITIVE, donor, ComponentType.RED_BLOOD_CELLS),
                    "AB+ should receive from all donors for RBC, failed for " + donor);
        }
    }

    @Test
    @DisplayName("RBC: A+ cannot receive from B+ or AB+")
    void testIncompatibleRbc() {
        assertFalse(policy.isCompatible(BloodGroup.A_POSITIVE, BloodGroup.B_POSITIVE, ComponentType.RED_BLOOD_CELLS));
        assertFalse(policy.isCompatible(BloodGroup.A_POSITIVE, BloodGroup.AB_POSITIVE, ComponentType.RED_BLOOD_CELLS));
    }

    @Test
    @DisplayName("Plasma: AB is universal plasma donor")
    void testUniversalPlasmaDonor() {
        assertTrue(policy.isCompatible(BloodGroup.O_POSITIVE, BloodGroup.AB_POSITIVE, ComponentType.FRESH_FROZEN_PLASMA));
        assertTrue(policy.isCompatible(BloodGroup.A_POSITIVE, BloodGroup.AB_POSITIVE, ComponentType.FRESH_FROZEN_PLASMA));
        assertTrue(policy.isCompatible(BloodGroup.B_POSITIVE, BloodGroup.AB_POSITIVE, ComponentType.FRESH_FROZEN_PLASMA));
    }

    @Test
    @DisplayName("Exact ABO/Rh match receives maximum score of 100")
    void testExactMatchScore() {
        int score = policy.getCompatibilityScore(BloodGroup.B_POSITIVE, BloodGroup.B_POSITIVE, ComponentType.RED_BLOOD_CELLS);
        assertEquals(100, score);
        assertTrue(policy.getCompatibilityExplanation(BloodGroup.B_POSITIVE, BloodGroup.B_POSITIVE, ComponentType.RED_BLOOD_CELLS).contains("Exact"));
    }
}
