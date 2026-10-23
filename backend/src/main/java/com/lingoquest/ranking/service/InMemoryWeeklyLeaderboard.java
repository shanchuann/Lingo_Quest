package com.lingoquest.ranking.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process weekly leaderboard for MySQL-only deployments
 * (`REDIS_ENABLED=false`). Ranking data resets on restart and is not shared
 * between instances — fine for a single-instance course demo.
 */
@Component
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "false")
public class InMemoryWeeklyLeaderboard implements WeeklyLeaderboard {

    private final ConcurrentHashMap<Long, Integer> scores = new ConcurrentHashMap<>();
    private volatile String week = WeeklyKeys.current();

    private void rolloverIfNeeded() {
        var now = WeeklyKeys.current();
        if (!now.equals(week)) {
            synchronized (this) {
                if (!now.equals(week)) {
                    scores.clear();
                    week = now;
                }
            }
        }
    }

    @Override
    public synchronized void record(Long userId, int gainedExp) {
        rolloverIfNeeded();
        scores.merge(userId, gainedExp, Integer::sum);
    }

    @Override
    public List<Score> top(int limit) {
        rolloverIfNeeded();
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(Math.max(1, limit))
                .map(e -> new Score(e.getKey(), e.getValue()))
                .toList();
    }
}
