package com.challenge.aguiabranca.api.common.error;

import org.springframework.http.HttpStatus;

public class ResourceConflictException extends ApiException {
    public ResourceConflictException(String code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
}

