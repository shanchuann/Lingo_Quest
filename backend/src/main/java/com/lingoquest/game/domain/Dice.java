package com.lingoquest.game.domain;

import java.util.concurrent.ThreadLocalRandom;

public final class Dice {
    private Dice() {}

    public static int roll() {
        return ThreadLocalRandom.current().nextInt(1, 7);
    }
}
