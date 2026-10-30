/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Core aggregate root representing a payment instrument (Credit Card or UPI credit-linked card).
 * Contains baseline reward parameters, active reward rules, and default monthly spend caps.
 *
 * Broad Architecture Placement:
 * Resides in the domain model layer of cardroute-engine-core; loaded via CardRepositoryPort
 * and evaluated by TransactionEvaluationService.
 */

package com.cardroute.engine.domain.model;

import com.cardroute.common.domain.PaymentInstrumentType;
import com.cardroute.common.domain.RewardType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Domain entity representing a card product specification.
 *
 * @param id Unique card identifier.
 * @param name Display product name (e.g. "Regalia Gold", "Infinia").
 * @param issuer Financial institution / bank name.
 * @param instrumentType Payment instrument classification.
 * @param baseRewardRate Baseline decimal reward rate (e.g. 0.01 for 1%).
 * @param rewardType Unit of reward yield.
 * @param monthlySpendCap Maximum monthly aggregate spend eligible for accelerated yield.
 * @param rules List of specific category and exclusion rules.
 */
public record CardProduct(
    String id,
    String name,
    String issuer,
    PaymentInstrumentType instrumentType,
    BigDecimal baseRewardRate,
    RewardType rewardType,
    BigDecimal monthlySpendCap,
    List<RewardRule> rules
) {
    public CardProduct {
        Objects.requireNonNull(id, "Card id cannot be null");
        Objects.requireNonNull(name, "Card name cannot be null");
        Objects.requireNonNull(issuer, "Issuer cannot be null");
        Objects.requireNonNull(instrumentType, "Instrument type cannot be null");
        baseRewardRate = baseRewardRate != null ? baseRewardRate : BigDecimal.ZERO;
        rewardType = rewardType != null ? rewardType : RewardType.CASHBACK_PERCENTAGE;
        monthlySpendCap = monthlySpendCap != null ? monthlySpendCap : BigDecimal.valueOf(100000);
        rules = rules != null ? Collections.unmodifiableList(rules) : Collections.emptyList();
    }
}

o
