package com.challenge.aguiabranca.api.common.error;

public record FieldViolation(String field, String code, String message) {}

