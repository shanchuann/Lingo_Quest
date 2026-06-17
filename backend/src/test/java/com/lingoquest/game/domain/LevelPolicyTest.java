package com.lingoquest.game.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Pure-domain test — no Spring context, no mocks. */
class LevelPolicyTest {

    @Test
    @DisplayName("0 xp is Lv 1 (Beginner)")
    void zeroXpIsLevel1() {
        assertThat(LevelPolicy.levelOf(0)).isEqualTo(1);
        assertThat(LevelPolicy.titleOf(1)).isEqualTo("Beginner");
    }

    @Test
    @DisplayName("Threshold boundaries map to expected levels")
    void thresholdsAreInclusive() {
        // 100 → Lv 2, 300 → Lv 3, 700 → Lv 4, 1500 → Lv 5
        assertThat(LevelPolicy.levelOf(99)).isEqualTo(1);
        assertThat(LevelPolicy.levelOf(100)).isEqualTo(2);
        assertThat(LevelPolicy.levelOf(299)).isEqualTo(2);
        assertThat(LevelPolicy.levelOf(300)).isEqualTo(3);
        assertThat(LevelPolicy.levelOf(699)).isEqualTo(3);
        assertThat(LevelPolicy.levelOf(700)).isEqualTo(4);
        assertThat(LevelPolicy.levelOf(1499)).isEqualTo(4);
        assertThat(LevelPolicy.levelOf(1500)).isEqualTo(5);
        assertThat(LevelPolicy.levelOf(9_999_999)).isEqualTo(5);
    }

    @Test
    @DisplayName("Titles map per level")
    void titles() {
        assertThat(LevelPolicy.titleOf(2)).isEqualTo("Learner");
        assertThat(LevelPolicy.titleOf(3)).isEqualTo("Explorer");
        assertThat(LevelPolicy.titleOf(4)).isEqualTo("Challenger");
        assertThat(LevelPolicy.titleOf(5)).isEqualTo("Master");
        assertThat(LevelPolicy.titleOf(99)).isEqualTo("Master"); // anything > 4 → Master
    }

    @Test
    @DisplayName("Board size constant locks the tile count at 30")
    void boardSize() {
        assertThat(LevelPolicy.BOARD_SIZE).isEqualTo(30);
    }
}
