/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Base abstract domain exception providing structured error typing, error codes,
 * and contextual details for RFC 7807 problem detail serialization.
 *
 * Broad Architecture Placement:
 * Sits in cardroute-common; all business, domain, and adapter exceptions inherit from this class.
 */

package com.cardroute.common.error;

import java.util.Collections;
import java.util.Map;

/**
 * Base domain exception for cardroute-engine.
 */
public abstract class CardRouteException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    /**
     * Constructs a new CardRouteException.
     *
     * @param errorCode The structured error code.
     * @param message Descriptive error message.
     */
    protected CardRouteException(ErrorCode errorCode, String message) {
        this(errorCode, message, Collections.emptyMap());
    }

    /**
     * Constructs a new CardRouteException with additional context properties.
     *
     * @param errorCode The structured error code.
     * @param message Descriptive error message.
     * @param details Key-value metadata for diagnostics.
     */
    protected CardRouteException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details != null ? Collections.unmodifiableMap(details) : Collections.emptyMap();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
