package com.lingoquest.game.dto;

public record GameView(
        Long id,
        Long userId,
        int currentPosition,
        int roundNum,
        String status,
        int totalScore
) {}
