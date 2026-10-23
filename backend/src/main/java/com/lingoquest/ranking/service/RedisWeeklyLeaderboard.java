package com.lingoquest.ranking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Redis ZSET implementation — the default path. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisWeeklyLeaderboard implements WeeklyLeaderboard {

    private final RedisTemplate<String, Object> redis;

    @Override
    public void record(Long userId, int gainedExp) {
        redis.opsForZSet().incrementScore(WeeklyKeys.current(), String.valueOf(userId), gainedExp);
    }

    @Override
    public List<Score> top(int limit) {
        var entries = redis.opsForZSet().reverseRangeWithScores(WeeklyKeys.current(), 0, Math.max(0, limit - 1));
        if (entries == null || entries.isEmpty()) return List.of();
        var result = new ArrayList<Score>(entries.size());
        for (var entry : entries) {
            if (entry.getValue() == null) continue;
            result.add(new Score(
                    Long.valueOf(entry.getValue().toString()),
                    entry.getScore() == null ? 0 : entry.getScore().intValue()));
        }
        return result;
    }
}
