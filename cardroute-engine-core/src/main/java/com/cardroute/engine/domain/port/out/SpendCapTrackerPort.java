/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Outbound port defining atomic spend-cap evaluation, decrement, and query operations.
 *
 * Broad Architecture Placement:
 * Outbound port in cardroute-engine-core; implemented by RedisSpendCapTrackerAdapter.
 */

package com.cardroute.engine.domain.port.out;

import com.cardroute.engine.domain.model.SpendCapAllocation;

import java.math.BigDecimal;

/**
 * Port contract for hot-path atomic spend cap tracking.
 */
public interface SpendCapTrackerPort {

    /**
     * Atomically checks remaining spend capacity for a card within a billing cycle and decrements
     * the allocated accelerated spend portion.
     *
     * @param cardId Unique card product identifier.
     * @param billingCycle Billing cycle period (e.g. "2026-10").
     * @param mccGroup MCC grouping identifier or category code.
     * @param requestedAmount Total transaction amount being evaluated.
     * @param totalCapLimit Baseline monthly cap configured for the card category.
     * @return SpendCapAllocation detailing accelerated vs overflow portions and remaining cap.
     */
    SpendCapAllocation allocateAtomicCap(
        String cardId,
        String billingCycle,
        String mccGroup,
        BigDecimal requestedAmount,
        BigDecimal totalCapLimit
    );

    /**
     * Reads the current remaining spend cap without mutating or decrementing it.
     *
     * @param cardId Unique card identifier.
     * @param billingCycle Billing cycle period.
     * @param mccGroup MCC grouping identifier.
     * @param totalCapLimit Total baseline limit if key has not yet been initialized.
     * @return Current remaining spend allowance.
     */
    BigDecimal getRemainingCap(
        String cardId,
        String billingCycle,
        String mccGroup,
        BigDecimal totalCapLimit
    );

    /**
     * Reverts or credits a previously allocated spend cap amount in case of transaction cancellation.
     *
     * @param cardId Unique card identifier.
     * @param billingCycle Billing cycle period.
     * @param mccGroup MCC category identifier.
     * @param amountToRevert Amount to return to the remaining capacity.
     * @param totalCapLimit Total maximum ceiling.
     */
    void rollbackCap(
        String cardId,
        String billingCycle,
        String mccGroup,
        BigDecimal amountToRevert,
        BigDecimal totalCapLimit
    );
}

