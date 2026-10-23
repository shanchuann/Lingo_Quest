package com.lingoquest.infra.ai;

import com.lingoquest.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process replacement for {@link RedisAiRateLimiter} when Redis is disabled.
 * Same per-user, per-minute quota; counters reset on JVM restart.
 */
@Component
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "false")
public class InMemoryAiRateLimiter implements AiRateLimiter {

    private record Window(long minute, int count) {}

    private final ConcurrentHashMap<Long, Window> windows = new ConcurrentHashMap<>();

    @Value("${app.ai.rate-limit-per-minute:10}")
    private int limitPerMinute;

    @Override
    public void check(Long userId) {
        if (userId == null) throw new BizException(40100, "Not authenticated");
        long minute = System.currentTimeMillis() / 60_000;
        var window = windows.compute(userId, (id, old) ->
                old == null || old.minute() != minute
                        ? new Window(minute, 1)
                        : new Window(minute, old.count() + 1));
        if (window.count() > limitPerMinute) {
            throw new BizException(42900, "Too many AI requests, please wait a minute");
        }
    }
}
