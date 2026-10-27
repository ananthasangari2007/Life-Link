package com.lifelink.inventory.model;

import com.lifelink.facility.model.Facility;
import com.lifelink.inventory.model.enums.TransactionType;
import com.lifelink.user.model.User;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Immutable audit ledger of every blood inventory change.
 */
@Entity
@Table(name = "lifelink_inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private BloodInventoryBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "quantity_changed", nullable = false)
    private int quantityChanged;

    @Column(name = "resulting_available_quantity", nullable = false)
    private int resultingAvailableQuantity;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public InventoryTransaction() {}

    public InventoryTransaction(BloodInventoryBatch batch, Facility facility, TransactionType transactionType,
                                int quantityChanged, int resultingAvailableQuantity, String referenceType,
                                Long referenceId, User actor, String reason) {
        this.batch = batch;
        this.facility = facility;
        this.transactionType = transactionType;
        this.quantityChanged = quantityChanged;
        this.resultingAvailableQuantity = resultingAvailableQuantity;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.actor = actor;
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public BloodInventoryBatch getBatch() {
        return batch;
    }

    public Facility getFacility() {
        return facility;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public int getQuantityChanged() {
        return quantityChanged;
    }

    public int getResultingAvailableQuantity() {
        return resultingAvailableQuantity;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public User getActor() {
        return actor;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
