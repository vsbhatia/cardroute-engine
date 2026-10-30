/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Inbound port interface declaring the contract for transaction reward routing evaluation.
 *
 * Broad Architecture Placement:
 * Sits in the hexagonal inbound port layer of cardroute-engine-core; implemented by
 * TransactionEvaluationService and consumed by REST and gRPC inbound adapters.
 */

package com.cardroute.engine.domain.port.in;

import com.cardroute.engine.domain.model.CardRouteDecision;
import com.cardroute.engine.domain.model.EvaluationContext;

import java.util.List;

/**
 * Inbound use case for evaluating and selecting the optimal payment instrument for a transaction.
 */
public interface EvaluateTransactionUseCase {

    /**
     * Evaluates candidate payment instruments, enforces atomic spend caps, and selects
     * the optimal card offering highest net reward yield.
     *
     * @param context Immutable transaction evaluation context.
     * @param candidateCardIds List of card IDs owned by the user.
     * @return The winning CardRouteDecision with complete yield breakdown.
     */
    CardRouteDecision evaluateOptimalCard(EvaluationContext context, List<String> candidateCardIds);
}

