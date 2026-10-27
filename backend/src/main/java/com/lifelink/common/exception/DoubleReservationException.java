package com.lifelink.common.exception;

public class DoubleReservationException extends LifeLinkException {
    public DoubleReservationException(String message) {
        super(message, "DOUBLE_RESERVATION_CONFLICT");
    }
}
