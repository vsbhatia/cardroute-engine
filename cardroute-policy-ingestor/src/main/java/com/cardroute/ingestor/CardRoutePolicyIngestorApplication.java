/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Main Spring Boot application entrypoint for cardroute-policy-ingestor.
 * Hosts the asynchronous PDF terms & conditions ingestion pipeline using Spring AI and pgvector.
 *
 * Broad Architecture Placement:
 * Sits at the root of cardroute-policy-ingestor. Strictly decoupled from the hot-path engine.
 */

package com.cardroute.ingestor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the policy ingestion pipeline.
 */
@SpringBootApplication(scanBasePackages = {"com.cardroute.ingestor", "com.cardroute.common"})
public class CardRoutePolicyIngestorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardRoutePolicyIngestorApplication.class, args);
    }
}

