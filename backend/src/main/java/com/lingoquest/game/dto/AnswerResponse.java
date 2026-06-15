package com.lingoquest.game.dto;

public record AnswerResponse(
        boolean correct,
        String correctAnswer,
        int gainedExp,
        int totalExp,
        int level,
        boolean levelUp
) {}
