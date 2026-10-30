/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Domain exception thrown when a transaction exceeds an immutable hard spend cap
 * where fallback yield is disabled or restricted.
 *
 * Broad Architecture Placement:
 * Sits in cardroute-common and is mapped to HTTP 422 in GlobalExceptionHandler.
 */

package com.cardroute.common.error;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Exception indicating that a card's category spend cap has been breached.
 */
public class SpendCapExceededException extends CardRouteException {

    public SpendCapExceededException(String cardId, String mcc, BigDecimal requestedAmount, BigDecimal remainingCap) {
        super(
            ErrorCode.SPEND_CAP_EXCEEDED,
            "Spend cap of remaining amount " + remainingCap + " exceeded by requested transaction " + requestedAmount,
            Map.of(
                "cardId", cardId,
                "mcc", mcc,
                "requestedAmount", requestedAmount,
                "remainingCap", remainingCap
            )
        );
    }
}

