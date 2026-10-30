/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Redis adapter implementing SpendCapTrackerPort using atomic Lua script execution.
 * Guarantees zero race conditions during parallel card swipe evaluations.
 *
 * Broad Architecture Placement:
 * Outbound adapter in cardroute-engine-core interacting with Redis cluster / standalone instances.
 */

package com.cardroute.engine.adapter.out.redis;

import com.cardroute.engine.domain.model.SpendCapAllocation;
import com.cardroute.engine.domain.port.out.SpendCapTrackerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Adapter executing atomic Redis operations for spend cap evaluation and deduction.
 */
@Component
public class RedisSpendCapTrackerAdapter implements SpendCapTrackerPort {

    private static final Logger log = LoggerFactory.getLogger(RedisSpendCapTrackerAdapter.class);
    private static final long DEFAULT_TTL_SECONDS = 2678400L; // 31 days
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List<String>> spendCapScript;

    /**
     * Constructor injection. Zero @Autowired.
     *
     * @param redisTemplate Spring StringRedisTemplate instance.
     * @param spendCapScript Pre-loaded Lua script bean.
     */
    public RedisSpendCapTrackerAdapter(
        StringRedisTemplate redisTemplate,
        RedisScript<List<String>> spendCapScript
    ) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate cannot be null");
        this.spendCapScript = Objects.requireNonNull(spendCapScript, "spendCapScript cannot be null");
    }

    @Override
    public SpendCapAllocation allocateAtomicCap(
        String cardId,
        String billingCycle,
        String mccGroup,
        BigDecimal requestedAmount,
        BigDecimal totalCapLimit
    ) {
        String key = buildSpendCapKey(cardId, billingCycle, mccGroup);

        List<String> keys = Collections.singletonList(key);
        String amountArg = requestedAmount.setScale(4, ROUNDING_MODE).toPlainString();
        String limitArg = totalCapLimit.setScale(4, ROUNDING_MODE).toPlainString();
        String ttlArg = String.valueOf(DEFAULT_TTL_SECONDS);

        List<String> result = redisTemplate.execute(spendCapScript, keys, amountArg, limitArg, ttlArg);

        if (result == null || result.size() < 3) {
            log.error("Redis Lua script execution returned invalid result for key={}", key);
            throw new IllegalStateException("Failed to execute atomic spend cap decrement in Redis");
        }

        BigDecimal remainingCapAfter = new BigDecimal(result.get(0)).setScale(2, ROUNDING_MODE);
        BigDecimal allocatedAccelerated = new BigDecimal(result.get(1)).setScale(2, ROUNDING_MODE);
        BigDecimal overflowAmount = new BigDecimal(result.get(2)).setScale(2, ROUNDING_MODE);

        boolean capExhausted = remainingCapAfter.compareTo(BigDecimal.ZERO) <= 0;

        return new SpendCapAllocation(allocatedAccelerated, overflowAmount, remainingCapAfter, capExhausted);
    }

    @Override
    public BigDecimal getRemainingCap(
        String cardId,
        String billingCycle,
        String mccGroup,
        BigDecimal totalCapLimit
    ) {
        String key = buildSpendCapKey(cardId, billingCycle, mccGroup);
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return totalCapLimit.setScale(2, ROUNDING_MODE);
        }

        return new BigDecimal(value).setScale(2, ROUNDING_MODE);
    }

    @Override
    public void rollbackCap(
        String cardId,
        String billingCycle,
        String mccGroup,
        BigDecimal amountToRevert,
        BigDecimal totalCapLimit
    ) {
        String key = buildSpendCapKey(cardId, billingCycle, mccGroup);
        String currentVal = redisTemplate.opsForValue().get(key);
        BigDecimal currentRemaining = currentVal != null ? new BigDecimal(currentVal) : totalCapLimit;
        BigDecimal newRemaining = currentRemaining.add(amountToRevert).min(totalCapLimit);

        redisTemplate.opsForValue().set(key, newRemaining.setScale(4, ROUNDING_MODE).toPlainString());
        log.info("Rolled back cap: key={} added={} newRemaining={}", key, amountToRevert, newRemaining);
    }

    private String buildSpendCapKey(String cardId, String billingCycle, String mccGroup) {
        return "spend_cap:" + cardId + ":" + billingCycle + ":" + mccGroup;
    }
}

