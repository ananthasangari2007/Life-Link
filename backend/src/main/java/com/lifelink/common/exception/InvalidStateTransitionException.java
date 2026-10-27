package com.lifelink.common.exception;

public class InvalidStateTransitionException extends LifeLinkException {
    public InvalidStateTransitionException(String entityName, String currentState, String attemptedState) {
        super(String.format("Invalid state transition for %s: cannot transition from [%s] to [%s].",
                entityName, currentState, attemptedState), "INVALID_STATE_TRANSITION");
    }

    public InvalidStateTransitionException(String message) {
        super(message, "INVALID_STATE_TRANSITION");
    }
}
