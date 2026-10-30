/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Represents the computed routing decision and reward yield for a given card candidate.
 * Contains detailed breakdown of base vs accelerated spend and net yield calculations.
 *
 * Broad Architecture Placement:
 * Domain outcome record returned by DeterministicRuleEvaluator and EvaluateTransactionUseCase.
 */

package com.cardroute.engine.domain.model;

import com.cardroute.common.domain.PaymentInstrumentType;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Evaluated routing outcome for a specific payment instrument.
 *
 * @param cardId Card product identifier.
 * @param cardName Card product display title.
 * @param instrumentType Instrument payment rail.
 * @param grossRewardAmount Absolute reward value earned before fee deductions.
 * @param netYieldPercentage Effective percentage yield on total swipe amount.
 * @param allocatedAcceleratedSpend Amount credited under accelerated category multipliers.
 * @param allocatedBaseSpend Amount credited under baseline rates.
 * @param remainingCapAfter Remaining monthly accelerated cap after this transaction.
 * @param isExclusionTriggered True if an exclusion rule disqualified this transaction.
 * @param recommendationScore Relative numeric score for ranking candidates (higher is better).
 * @param routingReason Human-readable explanation of why this card was or was not recommended.
 */
public record CardRouteDecision(
    String cardId,
    String cardName,
    PaymentInstrumentType instrumentType,
    BigDecimal grossRewardAmount,
    BigDecimal netYieldPercentage,
    BigDecimal allocatedAcceleratedSpend,
    BigDecimal allocatedBaseSpend,
    BigDecimal remainingCapAfter,
    boolean isExclusionTriggered,
    BigDecimal recommendationScore,
    String routingReason
) {
    public CardRouteDecision {
        Objects.requireNonNull(cardId, "cardId cannot be null");
        Objects.requireNonNull(cardName, "cardName cannot be null");
        Objects.requireNonNull(instrumentType, "instrumentType cannot be null");
        Objects.requireNonNull(grossRewardAmount, "grossRewardAmount cannot be null");
        Objects.requireNonNull(netYieldPercentage, "netYieldPercentage cannot be null");
        Objects.requireNonNull(allocatedAcceleratedSpend, "allocatedAcceleratedSpend cannot be null");
        Objects.requireNonNull(allocatedBaseSpend, "allocatedBaseSpend cannot be null");
        Objects.requireNonNull(remainingCapAfter, "remainingCapAfter cannot be null");
        Objects.requireNonNull(recommendationScore, "recommendationScore cannot be null");
        Objects.requireNonNull(routingReason, "routingReason cannot be null");
    }
}
