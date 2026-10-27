package com.lifelink;

import com.lifelink.common.exception.InsufficientInventoryException;
import com.lifelink.facility.model.Facility;
import com.lifelink.facility.model.enums.FacilityType;
import com.lifelink.inventory.model.BloodInventoryBatch;
import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import com.lifelink.inventory.model.enums.InventoryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Blood Inventory Batch Encapsulation & Invariants Tests")
class BloodInventoryBatchTest {

    private Facility facility;

    @BeforeEach
    void setUp() {
        facility = new Facility("Test Blood Bank", "LIC-TEST-001", FacilityType.BLOOD_BANK,
                "123 Test St", "Chennai", "Tamil Nadu", "600001", "+91 9999999999", "test@bb.com");
    }

    @Test
    @DisplayName("Batch available units accurately reflects reserved and dispatched quantities")
    void testAvailableQuantityCalculation() {
        Instant expiry = Instant.now().plus(30, ChronoUnit.DAYS);
        BloodInventoryBatch batch = new BloodInventoryBatch(
                facility, "BATCH-001", BloodGroup.O_POSITIVE, ComponentType.RED_BLOOD_CELLS,
                10, Instant.now(), expiry, "Rack A", "Test"
        );

        assertEquals(10, batch.getAvailableQuantity());

        // Reserve 4 units
        batch.reserve(4);
        assertEquals(6, batch.getAvailableQuantity());
        assertEquals(4, batch.getReservedQuantity());

        // Dispatch 3 units
        batch.dispatch(3);
        assertEquals(6, batch.getAvailableQuantity());
        assertEquals(1, batch.getReservedQuantity());
        assertEquals(3, batch.getDispatchedQuantity());

        // Release the remaining 1 reserved unit
        batch.releaseReservation(1);
        assertEquals(7, batch.getAvailableQuantity());
        assertEquals(0, batch.getReservedQuantity());
    }

    @Test
    @DisplayName("Reserving more units than available throws InsufficientInventoryException")
    void testOverAllocationFails() {
        Instant expiry = Instant.now().plus(30, ChronoUnit.DAYS);
        BloodInventoryBatch batch = new BloodInventoryBatch(
                facility, "BATCH-002", BloodGroup.A_POSITIVE, ComponentType.RED_BLOOD_CELLS,
                5, Instant.now(), expiry, "Rack A", "Test"
        );

        assertThrows(InsufficientInventoryException.class, () -> batch.reserve(6));
    }

    @Test
    @DisplayName("Quarantined batch returns 0 available units")
    void testQuarantinedBatchAvailability() {
        Instant expiry = Instant.now().plus(30, ChronoUnit.DAYS);
        BloodInventoryBatch batch = new BloodInventoryBatch(
                facility, "BATCH-003", BloodGroup.B_POSITIVE, ComponentType.RED_BLOOD_CELLS,
                10, Instant.now(), expiry, "Rack B", "Test"
        );

        assertEquals(10, batch.getAvailableQuantity());

        batch.quarantine("Failed initial screening");
        assertEquals(InventoryStatus.QUARANTINED, batch.getStatus());
        assertEquals(0, batch.getAvailableQuantity());
    }
}
