package com.lifelink.inventory.model;

import com.lifelink.common.entity.BaseEntity;
import com.lifelink.common.exception.InsufficientInventoryException;
import com.lifelink.facility.model.Facility;
import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import com.lifelink.inventory.model.enums.InventoryStatus;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Domain entity representing an audited batch of blood inventory.
 * Strict encapsulation guarantees inventory integrity:
 * - Available quantity is calculated as (quantity - reservedQuantity - dispatchedQuantity).
 * - Direct mutation of quantities via public setters is prohibited.
 * - Business methods reserve(), releaseReservation(), and dispatch() validate constraints.
 */
@Entity
@Table(name = "lifelink_blood_inventory_batches",
        uniqueConstraints = @UniqueConstraint(columnNames = {"facility_id", "batch_number"}))
public class BloodInventoryBatch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    private BloodGroup bloodGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    private ComponentType componentType;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity = 0;

    @Column(name = "dispatched_quantity", nullable = false)
    private int dispatchedQuantity = 0;

    @Column(name = "collection_date")
    private Instant collectionDate;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status = InventoryStatus.AVAILABLE;

    @Column(name = "storage_location")
    private String storageLocation;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public BloodInventoryBatch() {}

    public BloodInventoryBatch(Facility facility, String batchNumber, BloodGroup bloodGroup,
                               ComponentType componentType, int quantity, Instant collectionDate,
                               Instant expiryDate, String storageLocation, String notes) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Initial batch quantity must be positive.");
        }
        if (expiryDate.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot create an inventory batch with an expired date.");
        }
        this.facility = facility;
        this.batchNumber = batchNumber;
        this.bloodGroup = bloodGroup;
        this.componentType = componentType;
        this.quantity = quantity;
        this.reservedQuantity = 0;
        this.dispatchedQuantity = 0;
        this.collectionDate = collectionDate;
        this.expiryDate = expiryDate;
        this.storageLocation = storageLocation;
        this.notes = notes;
        this.status = InventoryStatus.AVAILABLE;
    }

    /**
     * Calculates the true available units remaining for new reservations.
     */
    public int getAvailableQuantity() {
        if (this.status != InventoryStatus.AVAILABLE || isExpired()) {
            return 0;
        }
        int available = this.quantity - (this.reservedQuantity + this.dispatchedQuantity);
        return Math.max(0, available);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }

    /**
     * Atomically reserves units from this batch.
     * Enforces invariants against negative inventory and over-allocation.
     */
    public void reserve(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("Reserved units must be greater than zero.");
        }
        if (this.status != InventoryStatus.AVAILABLE) {
            throw new IllegalStateException("Cannot reserve units from batch in status: " + this.status);
        }
        if (isExpired()) {
            this.status = InventoryStatus.EXPIRED;
            throw new IllegalStateException("Cannot reserve units from expired batch: " + this.batchNumber);
        }
        int available = getAvailableQuantity();
        if (units > available) {
            throw new InsufficientInventoryException(this.batchNumber, units, available);
        }
        this.reservedQuantity += units;
    }

    /**
     * Releases previously reserved units back to available stock.
     */
    public void releaseReservation(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("Units to release must be greater than zero.");
        }
        if (units > this.reservedQuantity) {
            throw new IllegalStateException(String.format(
                    "Cannot release %d units: only %d units are currently reserved in batch %s.",
                    units, this.reservedQuantity, this.batchNumber));
        }
        this.reservedQuantity -= units;
    }

    /**
     * Dispatches units, converting reserved stock to dispatched stock.
     */
    public void dispatch(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("Dispatched units must be greater than zero.");
        }
        if (units > this.reservedQuantity) {
            throw new IllegalStateException(String.format(
                    "Cannot dispatch %d units without prior reservation. Currently reserved: %d.",
                    units, this.reservedQuantity));
        }
        this.reservedQuantity -= units;
        this.dispatchedQuantity += units;

        if (this.dispatchedQuantity >= this.quantity) {
            this.status = InventoryStatus.DEPLETED;
        }
    }

    public void quarantine(String reason) {
        this.status = InventoryStatus.QUARANTINED;
        this.notes = (this.notes == null ? "" : this.notes + "; ") + "Quarantined: " + reason;
    }

    public void markAvailable() {
        if (isExpired()) {
            this.status = InventoryStatus.EXPIRED;
        } else if (this.dispatchedQuantity >= this.quantity) {
            this.status = InventoryStatus.DEPLETED;
        } else {
            this.status = InventoryStatus.AVAILABLE;
        }
    }

    // Read-only getters maintaining encapsulation
    public Facility getFacility() {
        return facility;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public int getDispatchedQuantity() {
        return dispatchedQuantity;
    }

    public Instant getCollectionDate() {
        return collectionDate;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public InventoryStatus getStatus() {
        if (this.status == InventoryStatus.AVAILABLE && isExpired()) {
            return InventoryStatus.EXPIRED;
        }
        return status;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
