/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Core application wiring configuration registering domain POJO beans.
 * Preserves Hexagonal purity by keeping domain service classes free from Spring annotations.
 *
 * Broad Architecture Placement:
 * Sits in cardroute-engine-core configuration layer.
 */

package com.cardroute.engine.config;

import com.cardroute.engine.domain.service.DeterministicRuleEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration wiring pure domain services into the application context.
 */
@Configuration
public class EngineConfig {

    /**
     * Registers the pure Java DeterministicRuleEvaluator domain service bean.
     *
     * @return Pure DeterministicRuleEvaluator instance.
     */
    @Bean
    public DeterministicRuleEvaluator deterministicRuleEvaluator() {
        return new DeterministicRuleEvaluator();
    }
}
