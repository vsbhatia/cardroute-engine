/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Represents a standardized 4-digit ISO 18245 Merchant Category Code (MCC).
 * This value object encapsulates MCC validation and categorization logic, ensuring
 * that invalid or malformed category codes cannot propagate into routing evaluation.
 *
 * Broad Architecture Placement:
 * Sits at the foundational layer in cardroute-common and is referenced by both the
 * hot-path routing engine and the policy ingestion modules.
 */

package com.cardroute.common.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing an ISO 18245 Merchant Category Code (MCC).
 *
 * <p>Merchant Category Codes are four-digit numeric codes assigned by payment networks
 * (Visa, Mastercard, RuPay) to classify a merchant's primary line of business.
 *
 * @param code The 4-digit numeric code string.
 */
public record MerchantCategoryCode(String code) {

    private static final Pattern MCC_PATTERN = Pattern.compile("^\\d{4}$");

    /**
     * Compact constructor validating the 4-digit numeric specification.
     *
     * @param code The 4-digit MCC string.
     * @throws IllegalArgumentException if the code is null or does not conform to 4 digits.
     */
    public MerchantCategoryCode {
        Objects.requireNonNull(code, "MCC code cannot be null");
        if (!MCC_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException("MCC must be exactly 4 numeric digits. Received: " + code);
        }
    }

    /**
     * Factory method creating a MerchantCategoryCode instance.
     *
     * @param code The string representation.
     * @return Validated MerchantCategoryCode instance.
     */
    public static MerchantCategoryCode of(String code) {
        return new MerchantCategoryCode(code);
    }
}

