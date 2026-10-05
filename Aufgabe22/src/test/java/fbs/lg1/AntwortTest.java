package fbs.lg1;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AntwortTest {

    private Bewertung bewertung;

    @BeforeEach
    void prepare() {
        Hersteller hersteller = new Hersteller("Logitech", "support@logitech.com");
        Produkt maus = new Produkt("P-001", "Maus", new BigDecimal("19.99"), hersteller);
        Kunde kunde = new Kunde("K-001", "Marcel Schachner");
        bewertung = new Bewertung(kunde, maus, 4, "Gute Maus", true);
    }

    @Test
    void testInit() {
        Antwort antwort = new Antwort(bewertung, "Danke!");

        assertThat(antwort.showBewertung()).isSameAs(bewertung);
        assertThat(antwort.showText()).isEqualTo("Danke!");
        assertThat(antwort.showAntwortDatum()).isEqualTo(LocalDate.now());
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Antwort(null, "Danke!"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Antwort(bewertung, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Antwort(bewertung, " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
