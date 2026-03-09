package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class NumberGeneratorTest {

    @Test
    void generatedNumberIsInRange() {
        for (int i = 0; i < 100; i++) {
            assertThat(NumberGenerator.generateNumber(1, 100)).isBetween(1, 100);
        }
    }

    @Test
    void generatedNumberRespectsBoundaries() {
        for (int i = 0; i < 100; i++) {
            assertThat(NumberGenerator.generateNumber(10, 50)).isBetween(10, 50);
        }
    }

    @Test
    void generatedNumberWithDifferentRanges() {
        assertThat(NumberGenerator.generateNumber(5, 15)).isBetween(5, 15);
        assertThat(NumberGenerator.generateNumber(-100, 0)).isBetween(-100, 0);
        assertThat(NumberGenerator.generateNumber(1000, 2000)).isBetween(1000, 2000);
    }

    @Test
    void identicalBoundaries() {
        assertThat(NumberGenerator.generateNumber(50, 50)).isEqualTo(50);
    }

    @Test
    void negativeBoundaries() {
        assertThat(NumberGenerator.generateNumber(-100, -50)).isBetween(-100, -50);
    }

    @Test
    void binarySearchGuessReturnsMiddle() {
        assertThat(NumberGenerator.binarySearchGuess(1, 100)).isEqualTo(50);
    }

    @Test
    void binarySearchGuessWithEvenRange() {
        assertThat(NumberGenerator.binarySearchGuess(1, 11)).isEqualTo(6);
    }

    @Test
    void binarySearchGuessWithIdenticalBoundaries() {
        assertThat(NumberGenerator.binarySearchGuess(50, 50)).isEqualTo(50);
    }

    @Test
    void binarySearchGuessWithNegativeBoundaries() {
        assertThat(NumberGenerator.binarySearchGuess(-100, -50)).isEqualTo(-75);
    }

    @Test
    void binarySearchGuessIsAlwaysInRange() {
        assertThat(NumberGenerator.binarySearchGuess(1, 100)).isBetween(1, 100);
        assertThat(NumberGenerator.binarySearchGuess(10, 20)).isBetween(10, 20);
    }
}
