package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class HumanGuessGameTest {

    HumanGuessGame game = new HumanGuessGame();

    @Test
    void correctGuess() {
        assertThat(game.compare(50, 50)).isEqualTo(0);
    }

    @Test
    void guessTooLow() {
        assertThat(game.compare(30, 50)).isEqualTo(-1);
    }

    @Test
    void guessTooHigh() {
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
