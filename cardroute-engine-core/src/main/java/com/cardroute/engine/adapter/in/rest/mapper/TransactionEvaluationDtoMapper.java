/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Pure mapping component converting between HTTP REST DTOs and internal domain models.
 *
 * Broad Architecture Placement:
 * Inbound REST adapter layer mapper in cardroute-engine-core.
 */

package com.cardroute.engine.adapter.in.rest.mapper;

import com.cardroute.common.domain.MerchantCategoryCode;
import com.cardroute.engine.adapter.in.rest.dto.TransactionEvaluationRequest;
import com.cardroute.engine.adapter.in.rest.dto.TransactionEvaluationResponse;
import com.cardroute.engine.domain.model.CardRouteDecision;
import com.cardroute.engine.domain.model.EvaluationContext;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mapper between external DTOs and internal domain representations.
 */
@Component
public class TransactionEvaluationDtoMapper {

    /**
     * Converts an inbound HTTP request DTO into an internal EvaluationContext.
     *
     * @param request Validated HTTP request payload.
     * @return Domain EvaluationContext instance.
     */
    public EvaluationContext toContext(TransactionEvaluationRequest request) {
        return new EvaluationContext(
            request.transactionId(),
            request.userId(),
            request.amount(),
            request.currency(),
            MerchantCategoryCode.of(request.mcc()),
            request.preferredInstrument(),
            request.billingCycle(),
            Instant.now()
        );
    }

    /**
     * Converts a domain CardRouteDecision into an outbound HTTP response DTO.
     *
     * @param transactionId Client transaction reference.
     * @param decision Domain decision aggregate.
     * @return Immutable TransactionEvaluationResponse.
     */
    public TransactionEvaluationResponse toResponse(String transactionId, CardRouteDecision decision) {
        return new TransactionEvaluationResponse(
            transactionId,
            decision.cardId(),
            decision.cardName(),
            decision.instrumentType(),
            decision.grossRewardAmount(),
            decision.netYieldPercentage(),
            decision.allocatedAcceleratedSpend(),
            decision.allocatedBaseSpend(),
            decision.remainingCapAfter(),
            decision.isExclusionTriggered(),
            decision.routingReason()
        );
    }
}

