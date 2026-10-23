package com.lingoquest.ranking.service;

import java.util.List;

/**
 * Weekly XP leaderboard abstraction.
 *
 * <p>Redis ZSET is used when available; a MySQL-only deployment falls back to
 * an in-process ranking that is rebuilt from answers after each restart.</p>
 */
public interface WeeklyLeaderboard {

    void record(Long userId, int gainedExp);

    /** Highest scores first, capped at {@code limit}. */
    List<Score> top(int limit);

    record Score(Long userId, int score) {}
}
