package com.lifelink.common.exception;

import java.time.Instant;
import java.util.Map;

/**
 * RFC 7807 problem details representation for consistent API error messages.
 */
public record ErrorResponse(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    String errorCode,
    Instant timestamp,
    Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String title, String detail, String errorCode, String instance) {
        return new ErrorResponse("urn:lifelink:problem:" + errorCode.toLowerCase().replace('_', '-'),
                title, status, detail, instance, errorCode, Instant.now(), null);
    }

    public static ErrorResponse ofValidation(int status, String detail, Map<String, String> fieldErrors, String instance) {
        return new ErrorResponse("urn:lifelink:problem:validation-error",
                "Validation Error", status, detail, instance, "VALIDATION_FAILED", Instant.now(), fieldErrors);
    }
}
