package com.lifelink.common.exception;

public class InsufficientInventoryException extends LifeLinkException {
    public InsufficientInventoryException(String batchNumber, int requested, int available) {
        super(String.format("Insufficient stock in batch [%s]: requested %d units, but only %d units are currently available.",
                batchNumber, requested, available), "INSUFFICIENT_INVENTORY");
    }

    public InsufficientInventoryException(String message) {
        super(message, "INSUFFICIENT_INVENTORY");
    }
}
