/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Spring configuration registering the atomic spend-cap decrement Lua script.
 *
 * Broad Architecture Placement:
 * Configuration component in cardroute-engine-core adapter layer.
 */

package com.cardroute.engine.adapter.out.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * Redis script configuration and bean registration.
 */
@Configuration
public class RedisScriptConfig {

    /**
     * Loads and registers the atomic spend cap decrement Lua script.
     *
     * @return Compiled RedisScript instance returning a list of strings.
     */
    @Bean
    @SuppressWarnings("unchecked")
    public RedisScript<List<String>> spendCapScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/atomic_spend_cap_decrement.lua"));
        script.setResultType(List.class);
        return (RedisScript<List<String>>) (RedisScript<?>) script;
    }
}

