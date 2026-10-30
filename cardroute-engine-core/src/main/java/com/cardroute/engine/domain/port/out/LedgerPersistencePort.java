/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Outbound port defining durable persistence operations for transaction spend and reward ledgers.
 *
 * Broad Architecture Placement:
 * Outbound port in cardroute-engine-core; implemented by PostgreSqlLedgerAdapter.
 */

package com.cardroute.engine.domain.port.out;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Port contract for persisting historical spend and reward records for reconciliation and audit.
 */
public interface LedgerPersistencePort {

    /**
     * Persists an immutable ledger record of an evaluated and routed transaction.
     *
     * @param transactionId Unique transaction reference.
     * @param userId Paying cardholder identifier.
     * @param cardId Selected card identifier.
     * @param billingCycle Billing cycle period.
     * @param mcc Verified merchant category code.
     * @param totalAmount Total transaction amount.
     * @param acceleratedAmount Spend amount processed at accelerated multiplier.
     * @param grossRewardEarned Absolute reward earned.
     * @param recordedAt Transaction recorded timestamp.
     */
    void recordTransactionLedger(
        String transactionId,
        String userId,
        String cardId,
        String billingCycle,
        String mcc,
        BigDecimal totalAmount,
        BigDecimal acceleratedAmount,
        BigDecimal grossRewardEarned,
        Instant recordedAt
    );
}

