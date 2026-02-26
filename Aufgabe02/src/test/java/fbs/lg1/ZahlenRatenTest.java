package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class ZahlenRatenTest {

  ZahlenRaten game = new ZahlenRaten();

  @Test
  void generatedNumberIsInRange() {
    for (int i = 0; i < 100; i++) {
      int number = game.generateNumber();
      assertThat(number).isBetween(1, 100);
    }
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
