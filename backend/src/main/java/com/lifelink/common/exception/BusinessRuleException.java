package com.lifelink.common.exception;

public class BusinessRuleException extends LifeLinkException {
    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION");
    }

    public BusinessRuleException(String message, String errorCode) {
        super(message, errorCode);
    }
}
