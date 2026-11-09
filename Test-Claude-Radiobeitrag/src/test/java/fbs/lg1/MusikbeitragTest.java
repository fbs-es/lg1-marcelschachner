package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MusikbeitragTest {

    private Musikbeitrag musik;

    @BeforeEach
    void prepare() {
        musik = new Musikbeitrag("Wolkenlos", 240, "Die Wolken", "Himmelblau", Musikgenre.POP);
    }

    @Test
    void testInit() {
        assertThat(musik.showTitel()).isEqualTo("Wolkenlos");
        assertThat(musik.showDauer()).isEqualTo(240);
        assertThat(musik.showStartzeitpunkt()).isEqualTo(0);
        assertThat(musik.showKuenstler()).isEqualTo("Die Wolken");
        assertThat(musik.showAlbum()).isEqualTo("Himmelblau");
        assertThat(musik.showGenre()).isEqualTo(Musikgenre.POP);
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Musikbeitrag(null, 240, "Die Wolken", "Himmelblau", Musikgenre.POP))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Musikbeitrag("Wolkenlos", 0, "Die Wolken", "Himmelblau", Musikgenre.POP))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Musikbeitrag("Wolkenlos", 240, " ", "Himmelblau", Musikgenre.POP))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Musikbeitrag("Wolkenlos", 240, "Die Wolken", null, Musikgenre.POP))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Musikbeitrag("Wolkenlos", 240, "Die Wolken", "Himmelblau", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testErstelleFortsetzung() {
        Radiobeitrag fortsetzung = musik.erstelleFortsetzung(100);

        assertThat(fortsetzung).isInstanceOf(Musikbeitrag.class);
        assertThat(fortsetzung.showTitel()).isEqualTo("Wolkenlos (Fortsetzung)");
        assertThat(fortsetzung.showDauer()).isEqualTo(100);
        assertThat(((Musikbeitrag) fortsetzung).showGenre()).isEqualTo(Musikgenre.POP);
    }
}
