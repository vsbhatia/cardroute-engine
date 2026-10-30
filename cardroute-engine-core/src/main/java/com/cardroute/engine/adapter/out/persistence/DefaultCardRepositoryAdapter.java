/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * In-memory / cache-backed adapter implementing CardRepositoryPort.
 * Provides pre-configured, production-like credit and UPI card definitions.
 *
 * Broad Architecture Placement:
 * Sits in the persistence/catalogue adapter layer of cardroute-engine-core.
 */

package com.cardroute.engine.adapter.out.persistence;

import com.cardroute.common.domain.PaymentInstrumentType;
import com.cardroute.common.domain.RewardType;
import com.cardroute.engine.domain.model.CardProduct;
import com.cardroute.engine.domain.model.RewardRule;
import com.cardroute.engine.domain.port.out.CardRepositoryPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Card catalogue adapter providing card products and rule definitions.
 */
@Component
public class DefaultCardRepositoryAdapter implements CardRepositoryPort {

    private final Map<String, CardProduct> cardCatalog = new ConcurrentHashMap<>();

    public DefaultCardRepositoryAdapter() {
        seedDefaultCatalog();
    }

    @Override
    public Optional<CardProduct> findById(String cardId) {
        return Optional.ofNullable(cardCatalog.get(cardId));
    }

    @Override
    public List<CardProduct> findCandidateCards(List<String> cardIds, PaymentInstrumentType instrumentType) {
        List<CardProduct> result = new ArrayList<>();
        for (String id : cardIds) {
            CardProduct product = cardCatalog.get(id);
            if (product != null) {
                if (instrumentType == null || product.instrumentType() == instrumentType) {
                    result.add(product);
                }
            }
        }
        return result;
    }

    /**
     * Seeds initial standard card products and reward rules.
     */
    private void seedDefaultCatalog() {
        // 1. HDFC Infinia: 3.3% base reward, 16.5% accelerated on dining & travel (MCC 5812, 4722), exclusion on Fuel (5541)
        RewardRule infiniaDining = new RewardRule(
            "RULE_INFINIA_DINING",
            "Dining 5X Multiplier",
            Set.of("5812", "5813", "5814"),
            new BigDecimal("0.165"),
            false,
            new BigDecimal("10000.00")
        );
        RewardRule infiniaFuelExclusion = new RewardRule(
            "RULE_INFINIA_FUEL_EXCLUSION",
            "Fuel Surcharge Exclusion",
            Set.of("5541", "5542"),
            BigDecimal.ZERO,
            true,
            null
        );
        CardProduct infinia = new CardProduct(
            "HDFC_INFINIA",
            "HDFC Infinia Metal",
            "HDFC Bank",
            PaymentInstrumentType.CREDIT_CARD,
            new BigDecimal("0.033"),
            RewardType.REWARD_POINTS,
            new BigDecimal("10000.00"),
            List.of(infiniaDining, infiniaFuelExclusion)
        );

        // 2. SBI Cashback Card: 1% base, 5% online shopping (MCC 5311, 5411, 5732), monthly cap 5000 INR
        RewardRule sbiOnlineShopping = new RewardRule(
            "RULE_SBI_ONLINE",
            "Online Retail 5% Cashback",
            Set.of("5311", "5411", "5732", "5999"),
            new BigDecimal("0.050"),
            false,
            new BigDecimal("5000.00")
        );
        RewardRule sbiRentWalletExclusion = new RewardRule(
            "RULE_SBI_EXCLUSION",
            "Rent and Wallet Load Exclusion",
            Set.of("6513", "6540"),
            BigDecimal.ZERO,
            true,
            null
        );
        CardProduct sbiCashback = new CardProduct(
            "SBI_CASHBACK",
            "SBI Cashback Card",
            "State Bank of India",
            PaymentInstrumentType.CREDIT_CARD,
            new BigDecimal("0.010"),
            RewardType.CASHBACK_PERCENTAGE,
            new BigDecimal("5000.00"),
            List.of(sbiOnlineShopping, sbiRentWalletExclusion)
        );

        // 3. Tata Neu Infinity RuPay: 1.5% base, 5% on Tata Brands & UPI merchant payments (MCC 5411, 5812)
        RewardRule tataNeuUpi = new RewardRule(
            "RULE_TATA_NEU_UPI",
            "Tata Neu UPI 5% NeuCoins",
            Set.of("5411", "5812", "5912"),
            new BigDecimal("0.050"),
            false,
            new BigDecimal("3000.00")
        );
        CardProduct tataNeu = new CardProduct(
            "TATA_NEU_INFINITY",
            "Tata Neu Infinity RuPay",
            "HDFC Bank",
            PaymentInstrumentType.UPI_CREDIT_LINKED,
            new BigDecimal("0.015"),
            RewardType.REWARD_POINTS,
            new BigDecimal("3000.00"),
            List.of(tataNeuUpi)
        );

        cardCatalog.put(infinia.id(), infinia);
        cardCatalog.put(sbiCashback.id(), sbiCashback);
        cardCatalog.put(tataNeu.id(), tataNeu);
    }
}
