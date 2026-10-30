/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Immutable domain model representing a specific reward rule condition, including
 * target MCCs, accelerated rate multiplier, category-specific caps, and exclusion status.
 *
 * Broad Architecture Placement:
 * Sits in the domain model layer of cardroute-engine-core; utilized by DeterministicRuleEvaluator.
 */

package com.cardroute.engine.domain.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Domain entity representing an individual reward rule or exclusion criterion.
 *
 * @param ruleId Unique rule identifier.
 * @param name Descriptive rule name.
 * @param targetMccs Set of 4-digit MCC codes this rule applies to.
 * @param acceleratedRate Decimal multiplier for matching spend (e.g., 0.05 for 5%).
 * @param isExclusion If true, matching transactions earn 0 reward regardless of other rules.
 * @param categoryCap Optional category-specific monthly spend cap.
 */
public record RewardRule(
    String ruleId,
    String name,
    Set<String> targetMccs,
    BigDecimal acceleratedRate,
    boolean isExclusion,
    BigDecimal categoryCap
) {
    public RewardRule {
        Objects.requireNonNull(ruleId, "ruleId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        targetMccs = targetMccs != null ? Collections.unmodifiableSet(targetMccs) : Collections.emptySet();
        acceleratedRate = acceleratedRate != null ? acceleratedRate : BigDecimal.ZERO;
    }

    /**
     * Determines whether this rule matches the supplied MCC code.
     *
     * @param mcc The 4-digit MCC code.
     * @return true if the rule target set contains the MCC.
     */
    public boolean matchesMcc(String mcc) {
        return targetMccs.contains(mcc);
    }
}
