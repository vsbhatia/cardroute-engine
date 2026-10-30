/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Integration test verifying TransactionEvaluationService against live containerized
 * PostgreSQL and Redis instances. Specifically proves atomic concurrency safety
 * and race condition prevention during simultaneous card swipe evaluations.
 *
 * Broad Architecture Placement:
 * Integration test suite in cardroute-engine-core validating ports, adapters, and Lua scripts.
 */

package com.cardroute.engine.service;

import com.cardroute.common.domain.Currency;
import com.cardroute.common.domain.MerchantCategoryCode;
import com.cardroute.common.domain.PaymentInstrumentType;
import com.cardroute.engine.AbstractIntegrationTest;
import com.cardroute.engine.adapter.out.persistence.repository.CardSpendLedgerRepository;
import com.cardroute.engine.domain.model.CardRouteDecision;
import com.cardroute.engine.domain.model.EvaluationContext;
import com.cardroute.engine.domain.port.in.EvaluateTransactionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Live integration tests validating deterministic routing and atomic spend-cap tracking.
 */
class TransactionEvaluationServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EvaluateTransactionUseCase evaluateTransactionUseCase;

    @Autowired
    private CardSpendLedgerRepository ledgerRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        ledgerRepository.deleteAll();
        var keys = redisTemplate.keys("spend_cap:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("Should evaluate single transaction and correctly calculate accelerated reward")
    void shouldEvaluateSingleTransactionSuccessfully() {
        // Arrange
        String billingCycle = "2026-10";
        EvaluationContext context = new EvaluationContext(
            "TXN_SINGLE_001",
            "USER_101",
            new BigDecimal("2000.00"),
            Currency.INR,
            MerchantCategoryCode.of("5411"), // Online Retail eligible for 5% on SBI Cashback
            PaymentInstrumentType.CREDIT_CARD,
            billingCycle,
            Instant.now()
        );

        // Act: Evaluate with SBI Cashback candidate (monthly cap = 5000)
        CardRouteDecision decision = evaluateTransactionUseCase.evaluateOptimalCard(
            context,
            List.of("SBI_CASHBACK")
        );

        // Assert
        assertThat(decision.cardId()).isEqualTo("SBI_CASHBACK");
        assertThat(decision.allocatedAcceleratedSpend()).isEqualByComparingTo("2000.00");
        assertThat(decision.allocatedBaseSpend()).isEqualByComparingTo("0.00");
        // 5% on 2000 = 100 INR
        assertThat(decision.grossRewardAmount()).isEqualByComparingTo("100.00");
        assertThat(decision.netYieldPercentage()).isEqualByComparingTo("5.0000");
        assertThat(decision.remainingCapAfter()).isEqualByComparingTo("3000.00");

        // Verify PostgreSQL persistence
        assertThat(ledgerRepository.count()).isEqualTo(1);
        var ledgerEntry = ledgerRepository.findByTransactionId("TXN_SINGLE_001").orElseThrow();
        assertThat(ledgerEntry.getTotalAmount()).isEqualByComparingTo("2000.0000");
    }

    @Test
    @DisplayName("Should enforce atomic spend cap under 20 concurrent transactions with zero double-spending")
    void shouldEnforceSpendCapUnderConcurrentRequestsWithoutRaceConditions() throws Exception {
        // Arrange: SBI Cashback has a 5,000 INR accelerated cap limit.
        // We simulate 20 concurrent transactions of 1,000 INR each.
        // Total attempted spend = 20,000 INR.
        // Expected outcome: Exactly 5,000 INR accelerated spend, 15,000 INR overflow spend.
        int totalRequests = 20;
        BigDecimal transactionAmount = new BigDecimal("1000.00");
        String billingCycle = "2026-10-CONCURRENT";

        ExecutorService executorService = Executors.newFixedThreadPool(totalRequests);
        CountDownLatch readyLatch = new CountDownLatch(totalRequests);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<CardRouteDecision>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            final String txnId = "TXN_CONC_" + i + "_" + UUID.randomUUID().toString().substring(0, 8);
            futures.add(executorService.submit(() -> {
                readyLatch.countDown();
                // Await simultaneous release
                startLatch.await(5, TimeUnit.SECONDS);

                EvaluationContext context = new EvaluationContext(
                    txnId,
                    "USER_CONC",
                    transactionAmount,
                    Currency.INR,
                    MerchantCategoryCode.of("5411"),
                    PaymentInstrumentType.CREDIT_CARD,
                    billingCycle,
                    Instant.now()
                );

                return evaluateTransactionUseCase.evaluateOptimalCard(
                    context,
                    Collections.singletonList("SBI_CASHBACK")
                );
            }));
        }

        // Wait for all worker threads to report ready, then trigger simultaneous execution
        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        executorService.shutdown();
        boolean finished = executorService.awaitTermination(30, TimeUnit.SECONDS);
        assertThat(finished).isTrue();

        // Act: Collect and aggregate results
        BigDecimal totalAcceleratedAllocated = BigDecimal.ZERO;
        BigDecimal totalOverflowAllocated = BigDecimal.ZERO;

        for (Future<CardRouteDecision> future : futures) {
            CardRouteDecision decision = future.get();
            assertThat(decision).isNotNull();
            totalAcceleratedAllocated = totalAcceleratedAllocated.add(decision.allocatedAcceleratedSpend());
            totalOverflowAllocated = totalOverflowAllocated.add(decision.allocatedBaseSpend());
        }

        // Assert: Strict concurrency and cap verification
        // Total accelerated spend credited MUST equal exactly 5,000.00 INR (the cap)
        assertThat(totalAcceleratedAllocated)
            .as("Total accelerated spend allocated must not exceed the 5000 INR monthly cap")
            .isEqualByComparingTo("5000.00");

        // Total overflow spend must equal exactly 15,000.00 INR (20,000 - 5,000)
        assertThat(totalOverflowAllocated)
            .as("Remaining spend exceeding the cap must overflow to base rate")
            .isEqualByComparingTo("15000.00");

        // Verify remaining cap in Redis is exactly zero
        String redisKey = "spend_cap:SBI_CASHBACK:" + billingCycle + ":5411";
        String redisRemaining = redisTemplate.opsForValue().get(redisKey);
        assertThat(redisRemaining).isNotNull();
        assertThat(new BigDecimal(redisRemaining).setScale(2, RoundingMode.HALF_EVEN))
            .as("Redis remaining cap must be exhausted to exactly 0.00")
            .isEqualByComparingTo("0.00");

        // Verify PostgreSQL persistence durability: exactly 20 ledger records saved
        assertThat(ledgerRepository.count())
            .as("All 20 concurrent transactions must be persisted in PostgreSQL ledger")
            .isEqualTo(20);
    }
}

