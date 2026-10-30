/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Immutable domain context holding incoming transaction parameters for reward routing.
 *
 * Broad Architecture Placement:
 * Domain value object passed to EvaluateTransactionUseCase.
 */

package com.cardroute.engine.domain.model;

import com.cardroute.common.domain.Currency;
import com.cardroute.common.domain.MerchantCategoryCode;
import com.cardroute.common.domain.PaymentInstrumentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Transaction evaluation execution context.
 *
 * @param transactionId Unique client-provided transaction reference.
 * @param userId Unique identifier of the paying customer.
 * @param amount Positive monetary value of the swipe.
 * @param currency Transaction ISO currency.
 * @param mcc Verified 4-digit Merchant Category Code.
 * @param preferredInstrument Preferred rail or instrument type filter.
 * @param billingCycle Target billing period string (e.g. "2026-10").
 * @param initiatedAt Transaction timestamp.
 */
public record EvaluationContext(
    String transactionId,
    String userId,
    BigDecimal amount,
    Currency currency,
    MerchantCategoryCode mcc,
    PaymentInstrumentType preferredInstrument,
    String billingCycle,
    Instant initiatedAt
) {
    public EvaluationContext {
        Objects.requireNonNull(transactionId, "transactionId cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
        Objects.requireNonNull(mcc, "mcc cannot be null");
        Objects.requireNonNull(preferredInstrument, "preferredInstrument cannot be null");
        Objects.requireNonNull(billingCycle, "billingCycle cannot be null");
        initiatedAt = initiatedAt != null ? initiatedAt : Instant.now();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be strictly positive. Received: " + amount);
        }
    }
}
