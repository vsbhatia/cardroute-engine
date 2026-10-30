/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Enumerates supported ISO 4217 currency codes across the routing and ledger engine.
 *
 * Broad Architecture Placement:
 * Domain primitive in cardroute-common utilized across transaction models and monetary calculations.
 */

package com.cardroute.common.domain;

/**
 * Standardized ISO 4217 Currency codes supported by cardroute-engine.
 */
public enum Currency {
    INR,
    USD,
    EUR,
    GBP
}
