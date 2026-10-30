/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Central enumeration of domain, validation, and infrastructure error codes.
 * Ensures consistent machine-readable error responses across REST and gRPC interfaces.
 *
 * Broad Architecture Placement:
 * Sits in cardroute-common and forms the basis for RFC 7807 ProblemDetail resolution.
 */

package com.cardroute.common.error;

import org.springframework.http.HttpStatus;

/**
 * Standard machine-readable error codes mapped to HTTP status representations.
 */
public enum ErrorCode {
    CARD_NOT_FOUND("CARD_ROUTE_404_001", HttpStatus.NOT_FOUND, "Requested card product was not found"),
    SPEND_CAP_EXCEEDED("CARD_ROUTE_422_001", HttpStatus.UNPROCESSABLE_ENTITY, "Monthly spend cap exceeded"),
    INVALID_TRANSACTION_PAYLOAD("CARD_ROUTE_400_001", HttpStatus.BAD_REQUEST, "Transaction parameters are invalid"),
    EXCLUSION_RULE_TRIGGERED("CARD_ROUTE_422_002", HttpStatus.UNPROCESSABLE_ENTITY, "Transaction matches exclusion criteria"),
    CONCURRENCY_CONFLICT("CARD_ROUTE_409_001", HttpStatus.CONFLICT, "Concurrent state modification conflict"),
    INTERNAL_ENGINE_ERROR("CARD_ROUTE_500_001", HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal engine error");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultTitle;

    ErrorCode(String code, HttpStatus httpStatus, String defaultTitle) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultTitle = defaultTitle;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }
}
