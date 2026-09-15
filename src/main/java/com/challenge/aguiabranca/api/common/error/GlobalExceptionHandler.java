package com.challenge.aguiabranca.api.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String PROBLEM_BASE = "https://api.aguia-branca.local/problems/";

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ProblemDetail> api(ApiException ex, HttpServletRequest request) {
        return problem(ex.status(), ex.code(), ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldViolation> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getCode(), error.getDefaultMessage()))
                .toList();
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Um ou mais campos são inválidos.", request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> constraint(ConstraintViolationException ex, HttpServletRequest request) {
        List<FieldViolation> errors = ex.getConstraintViolations().stream()
                .map(error -> new FieldViolation(error.getPropertyPath().toString(), "ConstraintViolation", error.getMessage()))
                .toList();
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Um ou mais parâmetros são inválidos.", request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> malformed(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "O corpo da requisição é inválido.", request, null);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    ResponseEntity<ProblemDetail> duplicate(DuplicateKeyException ex, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", "Já existe um recurso com os mesmos dados únicos.", request, null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<ProblemDetail> optimistic(OptimisticLockingFailureException ex, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "STALE_VERSION", "O recurso foi alterado por outra requisição.", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> denied(AccessDeniedException ex, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Você não possui permissão para esta operação.", request, null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected(Exception ex, HttpServletRequest request) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Ocorreu um erro interno.", request, null);
    }

    public static ResponseEntity<ProblemDetail> problem(
            HttpStatus status, String code, String detail, HttpServletRequest request, List<FieldViolation> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(PROBLEM_BASE + code.toLowerCase().replace('_', '-')));
        problem.setTitle(status.getReasonPhrase());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", code);
        problem.setProperty("traceId", MDC.get("traceId"));
        if (errors != null && !errors.isEmpty()) problem.setProperty("errors", errors);
        return ResponseEntity.status(status).body(problem);
    }
}

