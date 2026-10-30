/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Main Spring Boot application entrypoint for cardroute-engine-core.
 * Initializes the microservice with Virtual Threads and scheduled background workers.
 *
 * Broad Architecture Placement:
 * Sits at the root of cardroute-engine-core.
 */

package com.cardroute.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application bootstrapping class.
 */
@SpringBootApplication(scanBasePackages = {"com.cardroute.engine", "com.cardroute.common"})
public class CardRouteEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardRouteEngineApplication.class, args);
    }
}
