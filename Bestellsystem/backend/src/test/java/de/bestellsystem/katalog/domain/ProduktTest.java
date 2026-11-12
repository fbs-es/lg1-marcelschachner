package de.bestellsystem.katalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ProduktTest {

    private static Produkt produktMitBestand(int lagerbestand) {
        return new Produkt("Stehlampe", Geld.ausCent(4999), lagerbestand);
    }

    @Test
    @DisplayName("US-02.1 AK1: Produkt mit Lagerbestand 3 ist verfügbar")
    void verfuegbarBeiBestand() {
        assertThat(produktMitBestand(3).istVerfuegbar()).isTrue();
    }

    @Test
    @DisplayName("US-02.1 AK2: Produkt mit Lagerbestand 0 ist nicht verfügbar")
    void nichtVerfuegbarOhneBestand() {
        assertThat(produktMitBestand(0).istVerfuegbar()).isFalse();
    }

    @Test
    @DisplayName("US-02.1 AK3: Bestand 3 reicht für Menge 3, nicht für Menge 4")
    void verfuegbarkeitFuerMenge() {
        Produkt produkt = produktMitBestand(3);

        assertThat(produkt.istVerfuegbar(3)).isTrue();
        assertThat(produkt.istVerfuegbar(4)).isFalse();
    }

    @ParameterizedTest(name = "Menge {0}")
    @ValueSource(ints = {0, -1})
    @DisplayName("US-02.1 AK4: Menge 0 oder negativ wird als ungültig abgelehnt")
    void ungueltigeMenge(int menge) {
        Produkt produkt = produktMitBestand(3);

        assertThatThrownBy(() -> produkt.istVerfuegbar(menge))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("menge");
    }

    @Test
    @DisplayName("US-02.1 AK5: Lagerbestand −1 setzen wird abgelehnt")
    void bestandNieNegativ() {
        Produkt produkt = produktMitBestand(3);

        assertThatThrownBy(() -> produkt.setzeLagerbestand(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lagerbestand");
        assertThat(produkt.getLagerbestand()).isEqualTo(3);
    }

    @Test
    void verfuegbarkeitspruefungAendertBestandNicht() {
        Produkt produkt = produktMitBestand(3);

        produkt.istVerfuegbar(2);

        assertThat(produkt.getLagerbestand()).isEqualTo(3);
    }

    @Test
    void lagerbestandSetzen() {
        Produkt produkt = produktMitBestand(0);

        produkt.setzeLagerbestand(5);

        assertThat(produkt.getLagerbestand()).isEqualTo(5);
        assertThat(produkt.istVerfuegbar()).isTrue();
    }

    @Test
    @DisplayName("I-7: Anfangsbestand darf nicht negativ sein")
    void anfangsbestandNieNegativ() {
        assertThatThrownBy(() -> produktMitBestand(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lagerbestand");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("D-15: Titel ist Pflicht")
    void titelPflicht(String titel) {
        assertThatThrownBy(() -> new Produkt(titel, Geld.ausCent(100), 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titel");
    }

    @Test
    @DisplayName("D-15: Titel wird getrimmt, 200 Zeichen erlaubt, 201 nicht")
    void titelLaenge() {
        assertThat(new Produkt("  Lampe  ", Geld.ausCent(100), 1).getTitel()).isEqualTo("Lampe");
        assertThat(new Produkt("x".repeat(200), Geld.ausCent(100), 1).getTitel()).hasSize(200);
        assertThatThrownBy(() -> new Produkt("x".repeat(201), Geld.ausCent(100), 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titel");
    }

    @Test
    @DisplayName("D-15: Listenpreis 0 erlaubt, negativ nicht")
    void listenpreisNieNegativ() {
        assertThat(new Produkt("Probe", Geld.NULL, 1).getListenpreis()).isEqualTo(Geld.NULL);
        assertThatThrownBy(() -> new Produkt("Probe", Geld.ausCent(-1), 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("listenpreis");
        assertThatThrownBy(() -> new Produkt("Probe", null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("listenpreis");
    }

    @Test
    void neuesProduktHatId() {
        assertThat(produktMitBestand(1).getId()).isNotNull();
    }

    @Test
    @DisplayName("US-04.1 AK2: Entnehmen senkt den Lagerbestand")
    void entnehmen() {
        Produkt produkt = produktMitBestand(10);

        produkt.entnehme(2);

        assertThat(produkt.getLagerbestand()).isEqualTo(8);
    }

    @Test
    @DisplayName("D-09: Entnehmen über den Bestand hinaus wird abgelehnt, Bestand bleibt")
    void entnehmenUeberBestand() {
        Produkt produkt = produktMitBestand(1);

        assertThatThrownBy(() -> produkt.entnehme(2))
                .isInstanceOf(KonfliktException.class)
                .hasMessageContaining("Stehlampe");
        assertThat(produkt.getLagerbestand()).isEqualTo(1);
    }

    @Test
    @DisplayName("D-10: Entnehmen verlangt eine Menge von mindestens 1")
    void entnehmenMengeMindestensEins() {
        assertThatThrownBy(() -> produktMitBestand(5).entnehme(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("US-02.2 AK1: Titel und Listenpreis werden ersetzt, Titel getrimmt")
    void stammdatenAendern() {
        Produkt produkt = produktMitBestand(3);

        produkt.aendereStammdaten("  Stehlampe Nova ", Geld.ausCent(5999));

        assertThat(produkt.getTitel()).isEqualTo("Stehlampe Nova");
        assertThat(produkt.getListenpreis()).isEqualTo(Geld.ausCent(5999));
        assertThat(produkt.getLagerbestand()).isEqualTo(3);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("US-02.2 AK2: leerer Titel wird abgelehnt, Produkt bleibt unverändert")
    void leererTitelBeimAendern(String titel) {
        Produkt produkt = produktMitBestand(3);

        assertThatThrownBy(() -> produkt.aendereStammdaten(titel, Geld.ausCent(5999)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(produkt.getTitel()).isEqualTo("Stehlampe");
        assertThat(produkt.getListenpreis()).isEqualTo(Geld.ausCent(4999));
    }

    @Test
    @DisplayName("US-02.2 AK2: Titel über 200 Zeichen oder negativer Preis wird abgelehnt")
    void ungueltigeStammdaten() {
        Produkt produkt = produktMitBestand(3);

        assertThatThrownBy(() -> produkt.aendereStammdaten("x".repeat(201), Geld.ausCent(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> produkt.aendereStammdaten("Lampe", Geld.ausCent(-1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> produkt.aendereStammdaten("Lampe", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(produkt.getTitel()).isEqualTo("Stehlampe");
        assertThat(produkt.getListenpreis()).isEqualTo(Geld.ausCent(4999));
    }
}
