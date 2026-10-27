package com.lifelink;

import com.lifelink.common.exception.InvalidStateTransitionException;
import com.lifelink.request.model.enums.RequestStatus;
import com.lifelink.request.statemachine.RequestStateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Request State Machine Validator Tests")
class RequestStateValidatorTest {

    private RequestStateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RequestStateValidator();
    }

    @Test
    @DisplayName("Valid transition path from SUBMITTED to MATCHING to AWAITING_BANK_RESPONSES")
    void testValidTransitions() {
        assertDoesNotThrow(() -> validator.validateTransition(RequestStatus.SUBMITTED, RequestStatus.MATCHING));
        assertDoesNotThrow(() -> validator.validateTransition(RequestStatus.MATCHING, RequestStatus.AWAITING_BANK_RESPONSES));
        assertDoesNotThrow(() -> validator.validateTransition(RequestStatus.AWAITING_BANK_RESPONSES, RequestStatus.ACCEPTED));
        assertDoesNotThrow(() -> validator.validateTransition(RequestStatus.ACCEPTED, RequestStatus.IN_TRANSFER));
        assertDoesNotThrow(() -> validator.validateTransition(RequestStatus.IN_TRANSFER, RequestStatus.FULFILLED));
    }

    @Test
    @DisplayName("Invalid transition from SUBMITTED directly to FULFILLED throws exception")
    void testInvalidTransitionThrows() {
        assertThrows(InvalidStateTransitionException.class,
                () -> validator.validateTransition(RequestStatus.SUBMITTED, RequestStatus.FULFILLED));
    }

    @Test
    @DisplayName("Terminal state FULFILLED cannot transition to any other state")
    void testTerminalState() {
        assertThrows(InvalidStateTransitionException.class,
                () -> validator.validateTransition(RequestStatus.FULFILLED, RequestStatus.CANCELLED));
    }
}
