package com.lifelink.transfer.statemachine;

import com.lifelink.common.exception.InvalidStateTransitionException;
import com.lifelink.transfer.model.enums.TransferStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Validates Blood Transfer state transitions against the formal custody state machine.
 */
@Component
public class TransferStateValidator {

    private static final Map<TransferStatus, Set<TransferStatus>> VALID_TRANSITIONS = Map.of(
            TransferStatus.PREPARED, Set.of(TransferStatus.DISPATCHED, TransferStatus.IN_TRANSIT, TransferStatus.CANCELLED),
            TransferStatus.DISPATCHED, Set.of(TransferStatus.IN_TRANSIT, TransferStatus.RECEIVED, TransferStatus.CANCELLED),
            TransferStatus.IN_TRANSIT, Set.of(TransferStatus.RECEIVED, TransferStatus.CANCELLED),
            TransferStatus.RECEIVED, Set.of(),
            TransferStatus.CANCELLED, Set.of()
    );

    public void validateTransition(TransferStatus currentStatus, TransferStatus nextStatus) {
        if (currentStatus == nextStatus) {
            return;
        }
        Set<TransferStatus> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(nextStatus)) {
            throw new InvalidStateTransitionException("BloodTransfer", currentStatus.name(), nextStatus.name());
        }
    }
}
