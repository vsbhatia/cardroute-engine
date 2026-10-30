/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Distinguishes the payment mechanism utilized during transaction routing, such as
 * Credit Cards, RuPay on UPI, and Debit Cards.
 *
 * Broad Architecture Placement:
 * Core domain classifier in cardroute-common that determines which reward rules and
 * rail constraints (e.g. credit card interchange vs UPI merchant zero-MDR rules) apply.
 */

package com.cardroute.common.domain;

/**
 * Payment rail and instrument classification.
 */
public enum PaymentInstrumentType {
    CREDIT_CARD,
    UPI_CREDIT_LINKED,
    DEBIT_CARD
}

