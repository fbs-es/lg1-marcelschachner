package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EilmeldungTest {

    private Eilmeldung eilmeldung;

    @BeforeEach
    void prepare() {
        eilmeldung = new Eilmeldung("Sturmwarnung", 30, 4, "Kärnten");
    }

    @Test
    void testInit() {
        assertThat(eilmeldung.showTitel()).isEqualTo("Sturmwarnung");
        assertThat(eilmeldung.showDauer()).isEqualTo(30);
        assertThat(eilmeldung.showDringlichkeitsstufe()).isEqualTo(4);
        assertThat(eilmeldung.showRegion()).isEqualTo("Kärnten");
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Eilmeldung("Sturmwarnung", 30, 0, "Kärnten"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Eilmeldung("Sturmwarnung", 30, 6, "Kärnten"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Eilmeldung("Sturmwarnung", 30, 4, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Eilmeldung("Sturmwarnung", 0, 4, "Kärnten"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testInitGrenzwerte() {
        Eilmeldung niedrig = new Eilmeldung("Hinweis", 10, 1, "Wien");
        Eilmeldung hoch = new Eilmeldung("Katastrophe", 10, 5, "Wien");

        assertThat(niedrig.showDringlichkeitsstufe()).isEqualTo(1);
        assertThat(hoch.showDringlichkeitsstufe()).isEqualTo(5);
    }

    @Test
    void testErstelleFortsetzung() {
        Radiobeitrag fortsetzung = eilmeldung.erstelleFortsetzung(15);

        assertThat(fortsetzung).isInstanceOf(Eilmeldung.class);
        assertThat(fortsetzung.showDauer()).isEqualTo(15);
        assertThat(((Eilmeldung) fortsetzung).showRegion()).isEqualTo("Kärnten");
    }
}
