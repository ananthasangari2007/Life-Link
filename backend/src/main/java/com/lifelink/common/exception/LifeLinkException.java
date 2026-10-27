package com.lifelink.common.exception;

/**
 * Root abstract exception for all LifeLink domain exceptions.
 * Demonstrates an OOP exception hierarchy.
 */
public abstract class LifeLinkException extends RuntimeException {
    private final String errorCode;

    protected LifeLinkException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected LifeLinkException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
