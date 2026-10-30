/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Outbound persistence adapter implementing LedgerPersistencePort.
 * Translates domain ledger commands into JPA entities and saves them to PostgreSQL.
 *
 * Broad Architecture Placement:
 * Sits in the persistence adapter layer of cardroute-engine-core.
 */

package com.cardroute.engine.adapter.out.persistence;

import com.cardroute.engine.adapter.out.persistence.entity.CardSpendLedgerEntity;
import com.cardroute.engine.adapter.out.persistence.repository.CardSpendLedgerRepository;
import com.cardroute.engine.domain.port.out.LedgerPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * PostgreSQL adapter persisting transaction spend records.
 */
@Component
public class PostgreSqlLedgerAdapter implements LedgerPersistencePort {

    private static final Logger log = LoggerFactory.getLogger(PostgreSqlLedgerAdapter.class);

    private final CardSpendLedgerRepository repository;

    /**
     * Constructor injection. Zero @Autowired.
     *
     * @param repository Spring Data JPA repository for ledger entities.
     */
    public PostgreSqlLedgerAdapter(CardSpendLedgerRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository cannot be null");
    }

    @Override
    @Transactional
    public void recordTransactionLedger(
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
        CardSpendLedgerEntity entity = new CardSpendLedgerEntity(
            transactionId,
            userId,
            cardId,
            billingCycle,
            mcc,
            totalAmount,
            acceleratedAmount,
            grossRewardEarned,
            recordedAt
        );

        repository.save(entity);
        log.debug("Persisted ledger record for transactionId={} cardId={}", transactionId, cardId);
    }
}
