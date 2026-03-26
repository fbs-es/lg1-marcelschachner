package fbs.lg1;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class KinoVerwaltungTest {

    @Test
    void testInit() {
        KinoVerwaltung kino = new KinoVerwaltung(3, 3);
        assertThat(kino.getSitzplatzStatus(0, 0)).isEqualTo(SitzplatzStatus.FREI);
        assertThat(kino.getSitzplatzStatus(0, 1)).isEqualTo(SitzplatzStatus.FREI);
        assertThat(kino.getSitzplatzStatus(0, 2)).isEqualTo(SitzplatzStatus.FREI);
        assertThat(kino.getSitzplatzStatus(1, 0)).isEqualTo(SitzplatzStatus.FREI);
        assertThat(kino.getSitzplatzStatus(2, 0)).isEqualTo(SitzplatzStatus.RESERVIERT);
        assertThat(kino.getSitzplatzStatus(2, 1)).isEqualTo(SitzplatzStatus.RESERVIERT);
        assertThat(kino.getSitzplatzStatus(2, 2)).isEqualTo(SitzplatzStatus.RESERVIERT);
    }

    @Test
    void testVormerken() {
        KinoVerwaltung kino = new KinoVerwaltung(3, 5);
        int[][] positionen = kino.vormerken(2);
        assertThat(positionen.length).isEqualTo(2);
        assertThat(kino.getSitzplatzStatus(positionen[0][0], positionen[0][1])).isEqualTo(SitzplatzStatus.VORGEMERKT);
        assertThat(kino.getSitzplatzStatus(positionen[1][0], positionen[1][1])).isEqualTo(SitzplatzStatus.VORGEMERKT);
    }

    @Test
    void testVormerkenNichtMoeglich() {
        KinoVerwaltung kino = new KinoVerwaltung(2, 2);
        kino.vormerken(1);
        kino.vormerken(1);
        int[][] positionen = kino.vormerken(1);
        assertThat(positionen).isEmpty();
    }

    @Test
    void testBestaetigen() {
        KinoVerwaltung kino = new KinoVerwaltung(3, 5);
        int[][] positionen = kino.vormerken(2);
        kino.bestaetigen(positionen);
        assertThat(kino.getSitzplatzStatus(positionen[0][0], positionen[0][1])).isEqualTo(SitzplatzStatus.RESERVIERT);
        assertThat(kino.getSitzplatzStatus(positionen[1][0], positionen[1][1])).isEqualTo(SitzplatzStatus.RESERVIERT);
    }

    @Test
    void testAblehnen() {
        KinoVerwaltung kino = new KinoVerwaltung(3, 5);
        int[][] positionen = kino.vormerken(2);
        kino.ablehnen(positionen);
        assertThat(kino.getSitzplatzStatus(positionen[0][0], positionen[0][1])).isEqualTo(SitzplatzStatus.FREI);
        assertThat(kino.getSitzplatzStatus(positionen[1][0], positionen[1][1])).isEqualTo(SitzplatzStatus.FREI);
    }

    @Test
    void testVormerkenMitStartReiheUndStartSitz() {
        KinoVerwaltung kino = new KinoVerwaltung(5, 6);
        kino.vormerken(1, 0, 0);
        kino.vormerken(1, 0, 0);
        int[][] positionen = kino.vormerken(2, 0, 2);
        assertThat(positionen.length).isEqualTo(2);
        assertThat(positionen[0][0]).isEqualTo(0);
        assertThat(positionen[0][1]).isGreaterThanOrEqualTo(2);
        assertThat(kino.getSitzplatzStatus(positionen[0][0], positionen[0][1])).isEqualTo(SitzplatzStatus.VORGEMERKT);
        assertThat(kino.getSitzplatzStatus(positionen[1][0], positionen[1][1])).isEqualTo(SitzplatzStatus.VORGEMERKT);
    }

    @Test
    void testZaehle() {
        KinoVerwaltung kino = new KinoVerwaltung(3, 3);
        assertThat(kino.zaehle(SitzplatzStatus.FREI)).isEqualTo(6);
        assertThat(kino.zaehle(SitzplatzStatus.RESERVIERT)).isEqualTo(3);
        assertThat(kino.zaehle(SitzplatzStatus.VORGEMERKT)).isEqualTo(0);
        kino.vormerken(2);
        assertThat(kino.zaehle(SitzplatzStatus.FREI)).isEqualTo(4);
        assertThat(kino.zaehle(SitzplatzStatus.VORGEMERKT)).isEqualTo(2);
    }

    @Test
    void testVormerkenMitStartReiheNichtMoeglich() {
        KinoVerwaltung kino = new KinoVerwaltung(3, 3);
        int[][] positionen = kino.vormerken(1, 2, 0);
        assertThat(positionen).isEmpty();
    }

}
