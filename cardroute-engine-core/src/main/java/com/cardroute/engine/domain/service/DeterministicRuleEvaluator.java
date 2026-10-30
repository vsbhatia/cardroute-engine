/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Pure domain service executing deterministic reward arithmetic, MCC matching,
 * exclusion rule enforcement, and net yield calculation.
 *
 * Broad Architecture Placement:
 * Resides in the domain service layer of cardroute-engine-core. Completely free
 * of Spring or external framework dependencies to ensure deterministic, testable logic.
 */

package com.cardroute.engine.domain.service;

import com.cardroute.engine.domain.model.CardProduct;
import com.cardroute.engine.domain.model.CardRouteDecision;
import com.cardroute.engine.domain.model.EvaluationContext;
import com.cardroute.engine.domain.model.RewardRule;
import com.cardroute.engine.domain.model.SpendCapAllocation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Domain service executing deterministic reward calculations.
 */
public class DeterministicRuleEvaluator {

    private static final int DECIMAL_PRECISION = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /**
     * Evaluates a single card against transaction context and spend cap state.
     *
     * <p>Algorithm Flow:
     * 1. Inspect card rules for matching exclusion rules on the transaction MCC.
     *    If matched, return zero reward with exclusion flag set.
     * 2. Inspect card rules for matching accelerated category rules.
     * 3. Compute accelerated reward on allocatedAcceleratedSpend.
     * 4. Compute baseline reward on overflowSpend (spend exceeding accelerated cap).
     * 5. Sum total gross reward and derive net yield percentage.
     *
     * @param card Candidate card product.
     * @param context Inbound evaluation context.
     * @param capAllocation Pre-computed or atomic spend-cap allocation result.
     * @return Fully populated CardRouteDecision.
     */
    public CardRouteDecision evaluate(
        CardProduct card,
        EvaluationContext context,
        SpendCapAllocation capAllocation
    ) {
        String mccCode = context.mcc().code();

        // 1. Check Exclusion Rules
        boolean hasExclusion = card.rules().stream()
            .filter(RewardRule::isExclusion)
            .anyMatch(rule -> rule.matchesMcc(mccCode));

        if (hasExclusion) {
            return new CardRouteDecision(
                card.id(),
                card.name(),
                card.instrumentType(),
                BigDecimal.ZERO.setScale(2, ROUNDING_MODE),
                BigDecimal.ZERO.setScale(DECIMAL_PRECISION, ROUNDING_MODE),
                BigDecimal.ZERO.setScale(2, ROUNDING_MODE),
                context.amount().setScale(2, ROUNDING_MODE),
                capAllocation.remainingCapAfter(),
                true,
                BigDecimal.ZERO,
                "Transaction MCC " + mccCode + " matches exclusion criteria for " + card.name()
            );
        }

        // 2. Locate Matching Accelerated Category Rule
        Optional<RewardRule> acceleratedRule = card.rules().stream()
            .filter(rule -> !rule.isExclusion())
            .filter(rule -> rule.matchesMcc(mccCode))
            .findFirst();

        BigDecimal acceleratedRate = acceleratedRule.map(RewardRule::acceleratedRate).orElse(card.baseRewardRate());
        BigDecimal baseRate = card.baseRewardRate();

        BigDecimal acceleratedSpend = capAllocation.allocatedAcceleratedSpend();
        BigDecimal baseSpend = capAllocation.overflowSpend();

        // 3. Compute Reward Sums
        BigDecimal acceleratedReward = acceleratedSpend.multiply(acceleratedRate);
        BigDecimal baseReward = baseSpend.multiply(baseRate);
        BigDecimal totalReward = acceleratedReward.add(baseReward).setScale(2, ROUNDING_MODE);

        // 4. Compute Effective Yield Percentage: (totalReward / amount) * 100
        BigDecimal netYieldPercentage = totalReward
            .divide(context.amount(), DECIMAL_PRECISION + 2, ROUNDING_MODE)
            .multiply(BigDecimal.valueOf(100))
            .setScale(DECIMAL_PRECISION, ROUNDING_MODE);

        // 5. Build Human-Readable Reasoning
        String reason;
        if (acceleratedRule.isPresent() && acceleratedSpend.compareTo(BigDecimal.ZERO) > 0) {
            reason = String.format(
                "Earned %s%% accelerated yield on %s and %s%% base on overflow %s",
                acceleratedRate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString(),
                acceleratedSpend.stripTrailingZeros().toPlainString(),
                baseRate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString(),
                baseSpend.stripTrailingZeros().toPlainString()
            );
        } else {
            reason = String.format(
                "Standard base reward yield of %s%% applied across entire spend",
                baseRate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString()
            );
        }

        return new CardRouteDecision(
            card.id(),
            card.name(),
            card.instrumentType(),
            totalReward,
            netYieldPercentage,
            acceleratedSpend,
            baseSpend,
            capAllocation.remainingCapAfter(),
            false,
            netYieldPercentage,
            reason
        );
    }
}

