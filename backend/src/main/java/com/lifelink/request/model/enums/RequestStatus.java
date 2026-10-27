package com.lifelink.request.model.enums;

public enum RequestStatus {
    DRAFT,
    SUBMITTED,
    MATCHING,
    AWAITING_BANK_RESPONSES,
    PARTIALLY_FULFILLED,
    ACCEPTED,
    IN_TRANSFER,
    FULFILLED,
    UNFULFILLED,
    CANCELLED,
    REJECTED;

    public boolean isTerminal() {
        return this == FULFILLED || this == UNFULFILLED || this == CANCELLED || this == REJECTED;
    }
}
