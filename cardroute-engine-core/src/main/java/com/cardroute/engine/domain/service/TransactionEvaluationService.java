/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Core application service orchestrating transaction evaluation, card ranking,
 * atomic spend-cap decrement execution, and asynchronous ledger persistence.
 *
 * Broad Architecture Placement:
 * Sits in the application service layer of cardroute-engine-core; implements
 * EvaluateTransactionUseCase and coordinates domain ports.
 */

package com.cardroute.engine.domain.service;

import com.cardroute.common.error.CardNotFoundException;
import com.cardroute.common.error.InvalidTransactionException;
import com.cardroute.engine.domain.model.CardProduct;
import com.cardroute.engine.domain.model.CardRouteDecision;
import com.cardroute.engine.domain.model.EvaluationContext;
import com.cardroute.engine.domain.model.SpendCapAllocation;
import com.cardroute.engine.domain.port.in.EvaluateTransactionUseCase;
import com.cardroute.engine.domain.port.out.CardRepositoryPort;
import com.cardroute.engine.domain.port.out.LedgerPersistencePort;
import com.cardroute.engine.domain.port.out.SpendCapTrackerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Application service orchestrating payment instrument routing and atomic spend cap deduction.
 */
@Service
public class TransactionEvaluationService implements EvaluateTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(TransactionEvaluationService.class);

    private final CardRepositoryPort cardRepositoryPort;
    private final SpendCapTrackerPort spendCapTrackerPort;
    private final LedgerPersistencePort ledgerPersistencePort;
    private final DeterministicRuleEvaluator ruleEvaluator;

    /**
     * Explicit constructor injection. Zero @Autowired field injection allowed.
     *
     * @param cardRepositoryPort Outbound port to retrieve card definitions.
     * @param spendCapTrackerPort Outbound port for atomic Redis cap tracking.
     * @param ledgerPersistencePort Outbound port for durable PostgreSQL ledger recording.
     * @param ruleEvaluator Pure domain service for arithmetic calculations.
     */
    public TransactionEvaluationService(
        CardRepositoryPort cardRepositoryPort,
        SpendCapTrackerPort spendCapTrackerPort,
        LedgerPersistencePort ledgerPersistencePort,
        DeterministicRuleEvaluator ruleEvaluator
    ) {
        this.cardRepositoryPort = Objects.requireNonNull(cardRepositoryPort, "cardRepositoryPort cannot be null");
        this.spendCapTrackerPort = Objects.requireNonNull(spendCapTrackerPort, "spendCapTrackerPort cannot be null");
        this.ledgerPersistencePort = Objects.requireNonNull(ledgerPersistencePort, "ledgerPersistencePort cannot be null");
        this.ruleEvaluator = Objects.requireNonNull(ruleEvaluator, "ruleEvaluator cannot be null");
    }

    /**
     * Evaluates candidates, executes atomic spend-cap decrement for the winning card,
     * and persists the transaction to the PostgreSQL ledger.
     *
     * @param context Inbound transaction evaluation context.
     * @param candidateCardIds List of eligible candidate card IDs.
     * @return The winning CardRouteDecision with complete yield breakdown.
     */
    @Override
    public CardRouteDecision evaluateOptimalCard(EvaluationContext context, List<String> candidateCardIds) {
        if (candidateCardIds == null || candidateCardIds.isEmpty()) {
            throw new InvalidTransactionException("At least one candidate card ID must be supplied for routing");
        }

        log.info(
            "Initiating routing evaluation for transactionId={} amount={} mcc={} candidateCount={}",
            context.transactionId(),
            context.amount(),
            context.mcc().code(),
            candidateCardIds.size()
        );

        List<CardProduct> candidates = cardRepositoryPort.findCandidateCards(candidateCardIds, context.preferredInstrument());
        if (candidates.isEmpty()) {
            throw new CardNotFoundException(candidateCardIds.getFirst());
        }

        // Phase 1: Simulate yield across all candidate cards using current non-mutating cap state
        CardProduct optimalCard = null;
        BigDecimal highestScore = BigDecimal.valueOf(-1);

        for (CardProduct candidate : candidates) {
            BigDecimal currentRemainingCap = spendCapTrackerPort.getRemainingCap(
                candidate.id(),
                context.billingCycle(),
                context.mcc().code(),
                candidate.monthlySpendCap()
            );

            // Compute simulated allocation
            BigDecimal acceleratedPossible = context.amount().min(currentRemainingCap);
            BigDecimal overflow = context.amount().subtract(acceleratedPossible);
            SpendCapAllocation simulatedAllocation = new SpendCapAllocation(
                acceleratedPossible,
                overflow,
                currentRemainingCap.subtract(acceleratedPossible),
                currentRemainingCap.compareTo(acceleratedPossible) <= 0
            );

            CardRouteDecision simulationDecision = ruleEvaluator.evaluate(candidate, context, simulatedAllocation);
            if (simulationDecision.recommendationScore().compareTo(highestScore) > 0) {
                highestScore = simulationDecision.recommendationScore();
                optimalCard = candidate;
            }
        }

        if (optimalCard == null) {
            optimalCard = candidates.getFirst();
        }

        // Phase 2: Execute atomic Lua decrement on the selected optimal card
        SpendCapAllocation atomicAllocation = spendCapTrackerPort.allocateAtomicCap(
            optimalCard.id(),
            context.billingCycle(),
            context.mcc().code(),
            context.amount(),
            optimalCard.monthlySpendCap()
        );

        // Phase 3: Final deterministic yield calculation with confirmed atomic allocation
        CardRouteDecision finalDecision = ruleEvaluator.evaluate(optimalCard, context, atomicAllocation);

        // Phase 4: Persist durable audit record to PostgreSQL ledger
        ledgerPersistencePort.recordTransactionLedger(
            context.transactionId(),
            context.userId(),
            optimalCard.id(),
            context.billingCycle(),
            context.mcc().code(),
            context.amount(),
            finalDecision.allocatedAcceleratedSpend(),
            finalDecision.grossRewardAmount(),
            context.initiatedAt()
        );

        log.info(
            "Completed routing evaluation: transactionId={} winningCard={} grossReward={} netYield={}% remainingCap={}",
            context.transactionId(),
            finalDecision.cardName(),
            finalDecision.grossRewardAmount(),
            finalDecision.netYieldPercentage(),
            finalDecision.remainingCapAfter()
        );

        return finalDecision;
    }
}

