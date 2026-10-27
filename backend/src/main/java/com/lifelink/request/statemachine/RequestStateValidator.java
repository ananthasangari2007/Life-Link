package com.lifelink.request.statemachine;

import com.lifelink.common.exception.InvalidStateTransitionException;
import com.lifelink.request.model.enums.RequestStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

/**
 * Validates Blood Request state transitions against the formal lifecycle state machine.
 */
@Component
public class RequestStateValidator {

    private static final Map<RequestStatus, Set<RequestStatus>> VALID_TRANSITIONS = Map.ofEntries(
            entry(RequestStatus.DRAFT, Set.of(RequestStatus.SUBMITTED, RequestStatus.CANCELLED)),
            entry(RequestStatus.SUBMITTED, Set.of(RequestStatus.MATCHING, RequestStatus.AWAITING_BANK_RESPONSES, RequestStatus.CANCELLED)),
            entry(RequestStatus.MATCHING, Set.of(RequestStatus.AWAITING_BANK_RESPONSES, RequestStatus.UNFULFILLED, RequestStatus.CANCELLED)),
            entry(RequestStatus.AWAITING_BANK_RESPONSES, Set.of(RequestStatus.PARTIALLY_FULFILLED, RequestStatus.ACCEPTED, RequestStatus.UNFULFILLED, RequestStatus.CANCELLED, RequestStatus.REJECTED)),
            entry(RequestStatus.PARTIALLY_FULFILLED, Set.of(RequestStatus.ACCEPTED, RequestStatus.IN_TRANSFER, RequestStatus.CANCELLED)),
            entry(RequestStatus.ACCEPTED, Set.of(RequestStatus.IN_TRANSFER, RequestStatus.CANCELLED)),
            entry(RequestStatus.IN_TRANSFER, Set.of(RequestStatus.FULFILLED, RequestStatus.CANCELLED)),
            entry(RequestStatus.FULFILLED, Set.of()),
            entry(RequestStatus.UNFULFILLED, Set.of()),
            entry(RequestStatus.CANCELLED, Set.of()),
            entry(RequestStatus.REJECTED, Set.of())
    );

    public void validateTransition(RequestStatus currentStatus, RequestStatus nextStatus) {
        if (currentStatus == nextStatus) {
            return;
        }
        Set<RequestStatus> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(nextStatus)) {
            throw new InvalidStateTransitionException("BloodRequest", currentStatus.name(), nextStatus.name());
        }
    }

    public boolean canTransition(RequestStatus currentStatus, RequestStatus nextStatus) {
        if (currentStatus == nextStatus) return true;
        return VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(nextStatus);
    }
}
