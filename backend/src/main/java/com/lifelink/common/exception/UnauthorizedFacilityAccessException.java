package com.lifelink.common.exception;

public class UnauthorizedFacilityAccessException extends LifeLinkException {
    public UnauthorizedFacilityAccessException(Long userId, Long facilityId) {
        super(String.format("User [%d] is not authorized to access or modify resources belonging to facility [%d].",
                userId, facilityId), "UNAUTHORIZED_FACILITY_ACCESS");
    }

    public UnauthorizedFacilityAccessException(String message) {
        super(message, "UNAUTHORIZED_FACILITY_ACCESS");
    }
}
