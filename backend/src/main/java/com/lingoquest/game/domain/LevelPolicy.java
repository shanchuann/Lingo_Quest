package com.lingoquest.game.domain;

public final class LevelPolicy {
    public static final int BOARD_SIZE = 30;
    public static final int EXP_RIGHT = 10;
    public static final int EXP_WRONG = 2;

    private static final int[] THRESHOLDS = {0, 100, 300, 700, 1500};

    private LevelPolicy() {}

    public static int levelOf(int exp) {
        for (int i = THRESHOLDS.length - 1; i >= 0; i--) {
            if (exp >= THRESHOLDS[i]) return i + 1;
        }
        return 1;
    }

    public static String titleOf(int level) {
        return switch (level) {
            case 1 -> "Beginner";
            case 2 -> "Learner";
            case 3 -> "Explorer";
            case 4 -> "Challenger";
            default -> "Master";
        };
    }
}
