package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class ZahlenRatenTest {

    ZahlenRaten game = new ZahlenRaten();

    @Test
    void generatedNumberIsInRange() {
        for (int i = 0; i < 100; i++) {
            int number = game.generateNumber(1, 100);
            assertThat(number).isBetween(1, 100);
        }
    }

    @Test
    void generatedNumberRespectsBoundaries() {
        int min = 10;
        int max = 50;
        for (int i = 0; i < 100; i++) {
            int number = game.generateNumber(min, max);
            assertThat(number).isBetween(min, max);
        }
    }

    @Test
    void generatedNumberWithDifferentRanges() {
        assertThat(game.generateNumber(5, 15)).isBetween(5, 15);
        assertThat(game.generateNumber(-100, 0)).isBetween(-100, 0);
        assertThat(game.generateNumber(1000, 2000)).isBetween(1000, 2000);
    }

    @Test
    void standardInterval() {
        for (int i = 0; i < 50; i++) {
            assertThat(game.generateNumber(1, 100)).isBetween(1, 100);
        }
    }

    @Test
    void shiftedInterval() {
        for (int i = 0; i < 50; i++) {
            assertThat(game.generateNumber(50, 100)).isBetween(50, 100);
        }
    }

    @Test
    void identicalBoundaries() {
        assertThat(game.generateNumber(50, 50)).isEqualTo(50);
    }

    @Test
    void negativeBoundaries() {
        assertThat(game.generateNumber(-100, -50)).isBetween(-100, -50);
    }

    @Test
    void correctGuess() {
        assertThat(game.compare(50, 50)).isEqualTo(0);
    }

    @Test
    void wrongGuess() {
        assertThat(game.compare(30, 50)).isEqualTo(-1);
        assertThat(game.compare(70, 50)).isEqualTo(1);
    }

    @Test
    void correctGuessAtBoundaries() {
        assertThat(game.compare(1, 1)).isEqualTo(0);
        assertThat(game.compare(100, 100)).isEqualTo(0);
    }

    @Test
    void closeGuessIsWarm() {
        assertThat(game.hint(50, 54)).isEqualTo("warm");
        assertThat(game.hint(54, 50)).isEqualTo("warm");
        assertThat(game.hint(50, 55)).isEqualTo("warm");
    }

    @Test
    void farGuessIsCold() {
        assertThat(game.hint(50, 57)).isEqualTo("kalt");
        assertThat(game.hint(57, 50)).isEqualTo("kalt");
        assertThat(game.hint(50, 56)).isEqualTo("kalt");
    }
}
