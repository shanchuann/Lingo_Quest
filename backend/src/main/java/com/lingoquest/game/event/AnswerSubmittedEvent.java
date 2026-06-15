package com.lingoquest.game.event;

/**
 * Domain event broadcast whenever a user finishes one answer.
 * Listeners: achievement (try unlock), ranking (update ZSET).
 */
public record AnswerSubmittedEvent(
        Long userId,
        Long gameId,
        Long questionId,
        String questionType,
        boolean correct,
        int gainedExp,
        int totalExpAfter,
        int levelAfter,
        int levelBefore
) {}
