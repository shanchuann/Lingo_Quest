package com.lingoquest.game.event;

/**
 * Domain event broadcast when a game finishes (reach END or abandon).
 */
public record GameFinishedEvent(
        Long userId,
        Long gameId,
        int totalScore,
        boolean reachedEnd
) {}
