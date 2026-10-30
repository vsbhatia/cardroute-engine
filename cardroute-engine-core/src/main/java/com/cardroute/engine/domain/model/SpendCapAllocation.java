/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Represents the outcome of an atomic spend-cap evaluation executed in Redis.
 * Captures how much of a transaction amount qualifies for accelerated reward rates
 * versus how much overflows to baseline or zero reward yield.
 *
 * Broad Architecture Placement:
 * Domain value object in cardroute-engine-core returned by SpendCapTrackerPort.
 */

package com.cardroute.engine.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Result record from an atomic spend-cap decrement execution.
 *
 * @param allocatedAcceleratedSpend Portion of spend within accelerated cap boundary.
 * @param overflowSpend Portion of spend that exceeds cap and falls back to base rate.
 * @param remainingCapAfter Remaining accelerated capacity after this transaction.
 * @param capExhausted Whether the accelerated cap is now completely exhausted (zero remaining).
 */
public record SpendCapAllocation(
    BigDecimal allocatedAcceleratedSpend,
    BigDecimal overflowSpend,
    BigDecimal remainingCapAfter,
    boolean capExhausted
) {
    public SpendCapAllocation {
        Objects.requireNonNull(allocatedAcceleratedSpend, "allocatedAcceleratedSpend cannot be null");
        Objects.requireNonNull(overflowSpend, "overflowSpend cannot be null");
        Objects.requireNonNull(remainingCapAfter, "remainingCapAfter cannot be null");
    }

    /**
     * Factory method creating a zero-allocation result for excluded transactions.
     *
     * @param transactionAmount Total amount evaluated.
     * @return SpendCapAllocation with zero accelerated spend and full overflow.
     */
    public static SpendCapAllocation fullOverflow(BigDecimal transactionAmount, BigDecimal remainingCap) {
        return new SpendCapAllocation(BigDecimal.ZERO, transactionAmount, remainingCap, remainingCap.compareTo(BigDecimal.ZERO) <= 0);
    }
}
