/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Outbound port interface defining retrieval operations for card product definitions.
 *
 * Broad Architecture Placement:
 * Outbound port in cardroute-engine-core domain; implemented by persistence or cache adapters.
 */

package com.cardroute.engine.domain.port.out;

import com.cardroute.common.domain.PaymentInstrumentType;
import com.cardroute.engine.domain.model.CardProduct;

import java.util.List;
import java.util.Optional;

/**
 * Port contract for querying card products and associated reward catalogues.
 */
public interface CardRepositoryPort {

    /**
     * Retrieves a single card product by its unique identifier.
     *
     * @param cardId Unique card product ID.
     * @return Optional containing the CardProduct if found.
     */
    Optional<CardProduct> findById(String cardId);

    /**
     * Retrieves all candidate cards for a user matching an optional instrument filter.
     *
     * @param cardIds Candidate card identifiers.
     * @param instrumentType Desired payment instrument type.
     * @return List of matching active CardProducts.
     */
    List<CardProduct> findCandidateCards(List<String> cardIds, PaymentInstrumentType instrumentType);
}
