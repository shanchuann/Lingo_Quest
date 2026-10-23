package com.lingoquest.infra;

import com.lingoquest.common.exception.BizException;
import com.lingoquest.infra.ai.InMemoryAiCache;
import com.lingoquest.infra.ai.InMemoryAiRateLimiter;
import com.lingoquest.ranking.service.InMemoryWeeklyLeaderboard;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Redis-free fallbacks: cache, rate limiter and weekly leaderboard. */
class InMemoryFallbackTest {

    @Test
    void cacheComputesOnceAndReusesEntry() {
        var cache = new InMemoryAiCache();
        ReflectionTestUtils.setField(cache, "ttlHours", 1);
        var calls = new AtomicInteger();

        var first = cache.cached("grammar", "I go school", String.class,
                () -> { calls.incrementAndGet(); return "fixed"; });
        var second = cache.cached("grammar", "I go school", String.class,
                () -> { calls.incrementAndGet(); return "other"; });

        assertEquals("fixed", first);
        assertEquals("fixed", second);
        assertEquals(1, calls.get());
    }

    @Test
    void cacheReloadsWhenEntryExpired() {
        var cache = new InMemoryAiCache();
        ReflectionTestUtils.setField(cache, "ttlHours", 0);
        var calls = new AtomicInteger();

        cache.cached("vocab", "abandon", String.class, () -> { calls.incrementAndGet(); return "v1"; });
        cache.cached("vocab", "abandon", String.class, () -> { calls.incrementAndGet(); return "v2"; });

        assertEquals(2, calls.get());
    }

    @Test
    void rateLimiterBlocksAfterQuota() {
        var limiter = new InMemoryAiRateLimiter();
        ReflectionTestUtils.setField(limiter, "limitPerMinute", 2);

        limiter.check(1L);
        limiter.check(1L);
        assertThrows(BizException.class, () -> limiter.check(1L));
        // another user has its own bucket
        limiter.check(2L);
    }

    @Test
    void weeklyLeaderboardAccumulatesAndOrdersScores() {
        var board = new InMemoryWeeklyLeaderboard();
        board.record(1L, 10);
        board.record(2L, 30);
        board.record(2L, 5);

        var top = board.top(10);

        assertEquals(2, top.size());
        assertEquals(2L, top.get(0).userId());
        assertEquals(35, top.get(0).score());
        assertEquals(1L, top.get(1).userId());
        assertEquals(10, top.get(1).score());
    }
}
