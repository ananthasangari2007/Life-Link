package com.lifelink.inventory.strategy;

import com.lifelink.inventory.model.BloodInventoryBatch;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * First-Expired-First-Out (FEFO/FIFO) allocation strategy.
 * Prioritizes units with the nearest expiration date to minimize wastage.
 */
@Component
public class FifoExpiryAllocationStrategy implements InventoryAllocationStrategy {

    @Override
    public List<BloodInventoryBatch> sortForAllocation(List<BloodInventoryBatch> candidateBatches) {
        if (candidateBatches == null) {
            return List.of();
        }
        return candidateBatches.stream()
                .filter(b -> b.getAvailableQuantity() > 0 && !b.isExpired())
                .sorted(Comparator.comparing(BloodInventoryBatch::getExpiryDate))
                .toList();
    }
}
