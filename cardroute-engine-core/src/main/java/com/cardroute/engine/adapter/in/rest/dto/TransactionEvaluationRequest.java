/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Immutable request DTO for the transaction routing evaluation endpoint.
 *
 * Broad Architecture Placement:
 * Inbound REST DTO in cardroute-engine-core. Enforces strict input validation at perimeter.
 */

package com.cardroute.engine.adapter.in.rest.dto;

import com.cardroute.common.domain.Currency;
import com.cardroute.common.domain.PaymentInstrumentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.List;

/**
 * Inbound HTTP request payload for evaluating optimal card routing.
 *
 * @param transactionId Unique client-assigned transaction identifier.
 * @param userId Unique cardholder ID.
 * @param amount Positive monetary transaction value.
 * @param currency ISO currency code.
 * @param mcc Standard 4-digit numeric Merchant Category Code.
 * @param preferredInstrument Optional instrument filter (e.g. CREDIT_CARD, UPI_CREDIT_LINKED).
 * @param billingCycle Billing cycle identifier string (e.g. "2026-10").
 * @param candidateCardIds List of card product IDs owned by the user.
 */
public record TransactionEvaluationRequest(
    @NotBlank(message = "transactionId is required")
    String transactionId,

    @NotBlank(message = "userId is required")
    String userId,

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than zero")
    BigDecimal amount,

    @NotNull(message = "currency is required")
    Currency currency,

    @NotBlank(message = "mcc is required")
    @Pattern(regexp = "^\\d{4}$", message = "mcc must be exactly 4 digits")
    String mcc,

    @NotNull(message = "preferredInstrument is required")
    PaymentInstrumentType preferredInstrument,

    @NotBlank(message = "billingCycle is required")
    String billingCycle,

    @NotEmpty(message = "candidateCardIds must contain at least one card ID")
    List<String> candidateCardIds
) {}
