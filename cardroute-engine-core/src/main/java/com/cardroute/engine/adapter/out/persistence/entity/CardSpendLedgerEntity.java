/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * JPA Entity mapping the durable card spend ledger table in PostgreSQL.
 * Provides historical auditability and reconciliation for all routed transactions.
 *
 * Broad Architecture Placement:
 * Sits in the persistence adapter layer of cardroute-engine-core.
 */

package com.cardroute.engine.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * JPA entity representing a ledger record in PostgreSQL.
 */
@Entity
@Table(name = "card_spend_ledger")
public class CardSpendLedgerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "card_id", nullable = false, length = 50)
    private String cardId;

    @Column(name = "billing_cycle", nullable = false, length = 20)
    private String billingCycle;

    @Column(name = "mcc", nullable = false, length = 10)
    private String mcc;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "accelerated_amount", nullable = false, precision = 15, scale = 4)
    private BigDecimal acceleratedAmount;

    @Column(name = "gross_reward_earned", nullable = false, precision = 15, scale = 4)
    private BigDecimal grossRewardEarned;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected CardSpendLedgerEntity() {
        // JPA specification requirement
    }

    public CardSpendLedgerEntity(
        String transactionId,
        String userId,
        String cardId,
        String billingCycle,
        String mcc,
        BigDecimal totalAmount,
        BigDecimal acceleratedAmount,
        BigDecimal grossRewardEarned,
        Instant recordedAt
    ) {
        this.transactionId = Objects.requireNonNull(transactionId);
        this.userId = Objects.requireNonNull(userId);
        this.cardId = Objects.requireNonNull(cardId);
        this.billingCycle = Objects.requireNonNull(billingCycle);
        this.mcc = Objects.requireNonNull(mcc);
        this.totalAmount = Objects.requireNonNull(totalAmount);
        this.acceleratedAmount = Objects.requireNonNull(acceleratedAmount);
        this.grossRewardEarned = Objects.requireNonNull(grossRewardEarned);
        this.recordedAt = recordedAt != null ? recordedAt : Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCardId() {
        return cardId;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public String getMcc() {
        return mcc;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getAcceleratedAmount() {
        return acceleratedAmount;
    }

    public BigDecimal getGrossRewardEarned() {
        return grossRewardEarned;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
