package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RadiobeitragTest {

    private Musikbeitrag musik;
    private Werbebeitrag werbung;
    private Moderation moderation;
    private Eilmeldung eilmeldung;

    @BeforeEach
    void prepare() {
        musik = new Musikbeitrag("Song", 200, "Künstler", "Album", "Pop");
        werbung = new Werbebeitrag("Spot", 30, "Firma", 500.0);
        moderation = new Moderation("Begrüßung", 60, "Guten Morgen!");
        eilmeldung = new Eilmeldung("Unwetter", 45, 3, "Kärnten");
    }

    @Test
    void testInit() {
        assertThat(musik.showTitel()).isEqualTo("Song");
        assertThat(musik.showDauer()).isEqualTo(200);
        assertThat(musik.showStartzeit()).isEqualTo(0);
        assertThat(musik.showEndzeit()).isEqualTo(200);
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Musikbeitrag("", 200, "Künstler", "Album", "Pop"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Musikbeitrag("Song", 0, "Künstler", "Album", "Pop"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Musikbeitrag("Song", 200, null, "Album", "Pop"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Musikbeitrag("Song", 200, "Künstler", " ", "Pop"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Musikbeitrag("Song", 200, "Künstler", "Album", ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Werbebeitrag("Spot", 30, "", 500.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Werbebeitrag("Spot", 30, "Firma", -1.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Moderation("Begrüßung", 60, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Eilmeldung("Unwetter", 45, 0, "Kärnten"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Eilmeldung("Unwetter", 45, 6, "Kärnten"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Eilmeldung("Unwetter", 45, 3, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testAendereDauer() {
        musik.aendereDauer(150);

        assertThat(musik.showDauer()).isEqualTo(150);
        assertThat(musik.showEndzeit()).isEqualTo(150);
    }

    @Test
    void testAendereDauerInvalid() {
        assertThatThrownBy(() -> musik.aendereDauer(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testAendereStartzeit() {
        musik.aendereStartzeit(100);

        assertThat(musik.showStartzeit()).isEqualTo(100);
        assertThat(musik.showEndzeit()).isEqualTo(300);
    }

    @Test
    void testAendereStartzeitInvalid() {
        assertThatThrownBy(() -> musik.aendereStartzeit(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testKopieMitDauer() {
        Radiobeitrag musikKopie = musik.kopieMitDauer(50);
        Radiobeitrag werbungKopie = werbung.kopieMitDauer(10);
        Radiobeitrag moderationKopie = moderation.kopieMitDauer(20);
        Radiobeitrag eilmeldungKopie = eilmeldung.kopieMitDauer(15);

        assertThat(musikKopie).isInstanceOf(Musikbeitrag.class);
        assertThat(musikKopie).isNotSameAs(musik);
        assertThat(musikKopie.showTitel()).isEqualTo("Song");
        assertThat(musikKopie.showDauer()).isEqualTo(50);
        assertThat(werbungKopie).isInstanceOf(Werbebeitrag.class);
        assertThat(werbungKopie.showDauer()).isEqualTo(10);
        assertThat(moderationKopie).isInstanceOf(Moderation.class);
        assertThat(moderationKopie.showDauer()).isEqualTo(20);
        assertThat(eilmeldungKopie).isInstanceOf(Eilmeldung.class);
        assertThat(eilmeldungKopie.showDauer()).isEqualTo(15);
    }

    @Test
    void testAendereDringlichkeitInvalid() {
        eilmeldung.aendereDringlichkeit(5);

        assertThatThrownBy(() -> eilmeldung.aendereDringlichkeit(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> eilmeldung.aendereDringlichkeit(6))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
