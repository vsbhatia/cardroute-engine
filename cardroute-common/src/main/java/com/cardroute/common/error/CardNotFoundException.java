/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Domain exception thrown when a referenced credit card or UPI instrument is not found in the registry.
 *
 * Broad Architecture Placement:
 * Sits in cardroute-common and is mapped to HTTP 404 in GlobalExceptionHandler.
 */

package com.cardroute.common.error;

import java.util.Map;

/**
 * Exception indicating that a requested card profile does not exist.
 */
public class CardNotFoundException extends CardRouteException {

    public CardNotFoundException(String cardId) {
        super(ErrorCode.CARD_NOT_FOUND, "Card product not found for id: " + cardId, Map.of("cardId", cardId));
    }
}

