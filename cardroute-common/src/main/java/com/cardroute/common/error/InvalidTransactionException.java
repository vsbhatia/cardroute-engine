/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Domain exception thrown when transaction amounts or parameters violate business invariants.
 *
 * Broad Architecture Placement:
 * Sits in cardroute-common and is mapped to HTTP 400 in GlobalExceptionHandler.
 */

package com.cardroute.common.error;

import java.util.Map;

/**
 * Exception indicating invalid transaction arguments or currency incompatibility.
 */
public class InvalidTransactionException extends CardRouteException {

    public InvalidTransactionException(String message) {
        super(ErrorCode.INVALID_TRANSACTION_PAYLOAD, message);
    }

    public InvalidTransactionException(String message, Map<String, Object> details) {
        super(ErrorCode.INVALID_TRANSACTION_PAYLOAD, message, details);
    }
}

