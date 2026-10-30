/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Spring Data JPA Repository interface for CardSpendLedgerEntity operations.
 *
 * Broad Architecture Placement:
 * Outbound persistence adapter repository in cardroute-engine-core.
 */

package com.cardroute.engine.adapter.out.persistence.repository;

import com.cardroute.engine.adapter.out.persistence.entity.CardSpendLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for ledger entries.
 */
@Repository
public interface CardSpendLedgerRepository extends JpaRepository<CardSpendLedgerEntity, Long> {

    Optional<CardSpendLedgerEntity> findByTransactionId(String transactionId);

    List<CardSpendLedgerEntity> findByCardIdAndBillingCycle(String cardId, String billingCycle);

    List<CardSpendLedgerEntity> findByUserId(String userId);
}

