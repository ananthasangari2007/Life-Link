package com.lifelink.inventory.strategy;

import com.lifelink.inventory.model.BloodInventoryBatch;

import java.util.List;

/**
 * Strategy interface for allocating blood batches.
 * Demonstrates the Strategy pattern and runtime polymorphism.
 */
public interface InventoryAllocationStrategy {
    /**
     * Orders candidate batches according to the allocation policy.
     */
    List<BloodInventoryBatch> sortForAllocation(List<BloodInventoryBatch> candidateBatches);
}
