package com.challenge.aguiabranca.api.common.error;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends ApiException {
    public BusinessRuleException(String code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
}

