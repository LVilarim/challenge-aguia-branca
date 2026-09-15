package com.challenge.aguiabranca.api.common.error;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resource) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", resource + " não encontrado.");
    }
}

