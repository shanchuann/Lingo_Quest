package com.lingoquest.infra.ai;

import com.lingoquest.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Sliding-window token bucket using Redis INCR + EXPIRE.
 * Per-user, per-minute. Keeps things simple — good enough for course demo.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisAiRateLimiter implements AiRateLimiter {
    private final RedisTemplate<String, Object> redis;

    @Value("${app.ai.rate-limit-per-minute}")
    private int limitPerMinute;

    @Override
    public void check(Long userId) {
        if (userId == null) throw new BizException(40100, "Not authenticated");
        var key = "lingo:ai:rl:" + userId + ":" + (System.currentTimeMillis() / 60_000);
        var count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, Duration.ofSeconds(70));
        }
        if (count != null && count > limitPerMinute) {
            throw new BizException(42900, "Too many AI requests, please wait a minute");
        }
    }
}
