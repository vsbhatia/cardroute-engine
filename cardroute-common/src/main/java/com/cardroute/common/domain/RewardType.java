/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Defines the reward value calculation unit for credit and payment products.
 *
 * Broad Architecture Placement:
 * Domain primitive in cardroute-common applied during yield evaluation and rule definition.
 */

package com.cardroute.common.domain;

/**
 * Supported reward denomination models.
 */
public enum RewardType {
    CASHBACK_PERCENTAGE,
    REWARD_POINTS,
    AIR_MILES
}

