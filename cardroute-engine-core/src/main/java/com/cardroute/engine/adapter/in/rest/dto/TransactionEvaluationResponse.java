/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Immutable response DTO returning the routing recommendation and reward breakdown.
 *
 * Broad Architecture Placement:
 * Outbound REST DTO returned by TransactionEvaluationController in cardroute-engine-core.
 */

package com.cardroute.engine.adapter.in.rest.dto;

import com.cardroute.common.domain.PaymentInstrumentType;

import java.math.BigDecimal;

/**
 * Outbound HTTP response payload conveying the optimal card recommendation.
 *
 * @param transactionId Reference transaction identifier.
 * @param recommendedCardId Selected optimal card product ID.
 * @param cardName Display name of selected card.
 * @param instrumentType Payment instrument rail.
 * @param grossRewardAmount Absolute reward value earned.
 * @param netYieldPercentage Effective percentage yield on total spend.
 * @param acceleratedSpend Spend credited under accelerated multiplier.
 * @param baseSpend Spend credited under baseline rate.
 * @param remainingCapAfter Remaining monthly accelerated cap after this transaction.
 * @param isExclusionTriggered Flag indicating if exclusion rule was triggered.
 * @param routingReason Human-readable justification of routing decision.
 */
public record TransactionEvaluationResponse(
    String transactionId,
    String recommendedCardId,
    String cardName,
    PaymentInstrumentType instrumentType,
    BigDecimal grossRewardAmount,
    BigDecimal netYieldPercentage,
    BigDecimal acceleratedSpend,
    BigDecimal baseSpend,
    BigDecimal remainingCapAfter,
    boolean isExclusionTriggered,
    String routingReason
) {}

