/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Central controller advice translating business, domain, and validation exceptions
 * into standardized RFC 7807 ProblemDetail responses.
 *
 * Broad Architecture Placement:
 * Inbound adapter layer component in cardroute-common that normalizes REST error payloads
 * across all HTTP API entrypoints.
 */

package com.cardroute.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller advice providing uniform RFC 7807 ProblemDetail error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String PROBLEM_BASE_URI = "https://api.cardroute.io/errors/";

    /**
     * Translates custom CardRouteException instances to RFC 7807 ProblemDetail.
     *
     * @param ex The captured domain exception.
     * @return Standardized ProblemDetail payload.
     */
    @ExceptionHandler(CardRouteException.class)
    public ProblemDetail handleCardRouteException(CardRouteException ex) {
        ErrorCode code = ex.getErrorCode();
        log.warn("Business exception caught: code={}, message={}", code.getCode(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(code.getHttpStatus(), ex.getMessage());
        problem.setTitle(code.getDefaultTitle());
        problem.setType(URI.create(PROBLEM_BASE_URI + code.getCode().toLowerCase()));
        problem.setProperty("errorCode", code.getCode());
        problem.setProperty("timestamp", Instant.now().toString());

        if (!ex.getDetails().isEmpty()) {
            problem.setProperty("details", ex.getDetails());
        }

        return problem;
    }

    /**
     * Translates validation failures from @Valid annotations into RFC 7807 ProblemDetail.
     *
     * @param ex Validation exception.
     * @return ProblemDetail with field-level constraint error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error on request: fieldErrorsCount={}", ex.getFieldErrorCount());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Request validation failed for one or more fields"
        );
        problem.setTitle("Validation Failure");
        problem.setType(URI.create(PROBLEM_BASE_URI + "validation-error"));
        problem.setProperty("errorCode", ErrorCode.INVALID_TRANSACTION_PAYLOAD.getCode());
        problem.setProperty("timestamp", Instant.now().toString());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        problem.setProperty("fieldErrors", fieldErrors);

        return problem;
    }

    /**
     * Catches unexpected throwables and maps them to generic 500 ProblemDetail.
     * Prevents internal stack traces from leaking to public clients.
     *
     * @param ex Unexpected exception.
     * @return Generic 500 ProblemDetail.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled server exception encountered", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected internal error occurred. Please refer to support."
        );
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create(PROBLEM_BASE_URI + "internal-error"));
        problem.setProperty("errorCode", ErrorCode.INTERNAL_ENGINE_ERROR.getCode());
        problem.setProperty("timestamp", Instant.now().toString());

        return problem;
    }
}
