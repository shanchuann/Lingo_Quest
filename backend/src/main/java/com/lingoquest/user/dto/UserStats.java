package com.lingoquest.user.dto;

public record UserStats(
        long totalGames,
        long finishedGames,
        long totalAnswers,
        long correctAnswers,
        double accuracy,
        int level,
        int experience,
        int coins
) {}
