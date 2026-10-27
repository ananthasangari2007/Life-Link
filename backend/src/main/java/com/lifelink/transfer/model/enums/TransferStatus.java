package com.lifelink.transfer.model.enums;

public enum TransferStatus {
    PREPARED,
    DISPATCHED,
    IN_TRANSIT,
    RECEIVED,
    CANCELLED;

    public boolean isTerminal() {
        return this == RECEIVED || this == CANCELLED;
    }
}
