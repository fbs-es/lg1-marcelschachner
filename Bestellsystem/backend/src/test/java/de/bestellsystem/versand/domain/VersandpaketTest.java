package de.bestellsystem.versand.domain;

import static de.bestellsystem.versand.domain.Lieferstatus.ANGELEGT;
import static de.bestellsystem.versand.domain.Lieferstatus.IN_ZUSTELLUNG;
import static de.bestellsystem.versand.domain.Lieferstatus.RETOURE;
import static de.bestellsystem.versand.domain.Lieferstatus.UEBERGEBEN;
import static de.bestellsystem.versand.domain.Lieferstatus.VERLOREN;
import static de.bestellsystem.versand.domain.Lieferstatus.ZUGESTELLT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class VersandpaketTest {

    private static final Instant AM_12_11 = Instant.parse("2026-11-12T09:30:00Z");
    private static final Instant AM_13_11 = Instant.parse("2026-11-13T08:00:00Z");

    @Test
    @DisplayName(
            "US-07.1 AK1: neues Paket ist ANGELEGT, angelegt und geändert zum selben Zeitpunkt")
    void neuesPaket() {
        Versandpaket paket = paket();

        assertThat(paket.getId()).isNotNull();
        assertThat(paket.getDienstleister()).isEqualTo(Logistikdienstleister.DHL);
        assertThat(paket.getSendungsnummer()).isEqualTo("00340434161094042557");
        assertThat(paket.getLieferstatus()).isEqualTo(ANGELEGT);
        assertThat(paket.getAngelegtAm()).isEqualTo(AM_12_11);
        assertThat(paket.getStatusGeaendertAm()).isEqualTo(AM_12_11);
    }

    @Test
    @DisplayName("US-07.1 AK4: Sendungsnummer wird getrimmt und großgeschrieben")
    void normalisiert() {
        assertThat(
                        new Versandpaket(Logistikdienstleister.GLS, " ab123de ", AM_12_11)
                                .getSendungsnummer())
                .isEqualTo("AB123DE");
        assertThat(Versandpaket.normalisiere(" ab123de ")).isEqualTo("AB123DE");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "AB-123", "AB 123", "ÄB123", "stra\u00dfe1"})
    @DisplayName("US-07.1 AK6: Sendungsnummer leer oder mit anderen Zeichen ist ungültig")
    void ungueltigeZeichen(String sendungsnummer) {
        assertThatThrownBy(
                        () -> new Versandpaket(Logistikdienstleister.DHL, sendungsnummer, AM_12_11))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("US-07.1 AK6: Sendungsnummer höchstens 50 Zeichen, Dienstleister erforderlich")
    void laengeUndDienstleister() {
        assertThat(new Versandpaket(Logistikdienstleister.DHL, "A".repeat(50), AM_12_11))
                .isNotNull();
        assertThatThrownBy(
                        () -> new Versandpaket(Logistikdienstleister.DHL, "A".repeat(51), AM_12_11))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Versandpaket(null, "AB123DE", AM_12_11))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Versandpaket(Logistikdienstleister.DHL, null, AM_12_11))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("US-07.2 AK1: nächste Stufe setzt Status und Änderungszeitpunkt, Anlage bleibt")
    void naechsteStufe() {
        Versandpaket paket = paket();

        paket.aendereStatus(UEBERGEBEN, AM_13_11);

        assertThat(paket.getLieferstatus()).isEqualTo(UEBERGEBEN);
        assertThat(paket.getStatusGeaendertAm()).isEqualTo(AM_13_11);
        assertThat(paket.getAngelegtAm()).isEqualTo(AM_12_11);
    }

    @Test
    @DisplayName("US-07.2 AK2: Stufen dürfen übersprungen werden")
    void ueberspringen() {
        Versandpaket paket = paket();

        paket.aendereStatus(ZUGESTELLT, AM_13_11);

        assertThat(paket.getLieferstatus()).isEqualTo(ZUGESTELLT);
    }

    @ParameterizedTest
    @EnumSource(
            value = Lieferstatus.class,
            names = {"UEBERGEBEN", "IN_ZUSTELLUNG"})
    @DisplayName("US-07.2 AK3: RETOURE und VERLOREN ab Übergabe möglich")
    void retoureUndVerlustNachUebergabe(Lieferstatus ausgang) {
        for (Lieferstatus ziel : List.of(RETOURE, VERLOREN)) {
            Versandpaket paket = paketIn(ausgang);

            paket.aendereStatus(ziel, AM_13_11);

            assertThat(paket.getLieferstatus()).isEqualTo(ziel);
        }
    }

    @ParameterizedTest
    @EnumSource(
            value = Lieferstatus.class,
            names = {"RETOURE", "VERLOREN"})
    @DisplayName("US-07.2 AK3: RETOURE und VERLOREN aus ANGELEGT abgelehnt")
    void retoureUndVerlustVorUebergabe(Lieferstatus ziel) {
        Versandpaket paket = paket();

        assertThatThrownBy(() -> paket.aendereStatus(ziel, AM_13_11))
                .isInstanceOf(FachlicheAblehnungException.class);
        assertThat(paket.getLieferstatus()).isEqualTo(ANGELEGT);
        assertThat(paket.getStatusGeaendertAm()).isEqualTo(AM_12_11);
    }

    @ParameterizedTest
    @EnumSource(
            value = Lieferstatus.class,
            names = {"ANGELEGT", "UEBERGEBEN", "IN_ZUSTELLUNG"})
    @DisplayName("US-07.2 AK4: nicht rückwärts, nicht derselbe Status")
    void nichtRueckwaerts(Lieferstatus ziel) {
        Versandpaket paket = paketIn(IN_ZUSTELLUNG);

        assertThatThrownBy(() -> paket.aendereStatus(ziel, AM_13_11))
                .isInstanceOf(FachlicheAblehnungException.class);
        assertThat(paket.getLieferstatus()).isEqualTo(IN_ZUSTELLUNG);
    }

    @ParameterizedTest
    @EnumSource(
            value = Lieferstatus.class,
            names = {"ZUGESTELLT", "RETOURE", "VERLOREN"})
    @DisplayName("US-07.2 AK5: aus einem Endzustand ist jeder Wechsel abgelehnt")
    void endzustaende(Lieferstatus endzustand) {
        Versandpaket paket = paketIn(endzustand);

        for (Lieferstatus ziel : Lieferstatus.values()) {
            assertThatThrownBy(() -> paket.aendereStatus(ziel, AM_13_11))
                    .isInstanceOf(FachlicheAblehnungException.class);
        }
        assertThat(paket.getLieferstatus()).isEqualTo(endzustand);
    }

    @Test
    @DisplayName("US-07.2 AK6: erlaubte Folgestatus je Status")
    void erlaubteFolgestatus() {
        assertThat(ANGELEGT.erlaubteFolgestatus())
                .containsExactly(UEBERGEBEN, IN_ZUSTELLUNG, ZUGESTELLT);
        assertThat(UEBERGEBEN.erlaubteFolgestatus())
                .containsExactly(IN_ZUSTELLUNG, ZUGESTELLT, RETOURE, VERLOREN);
        assertThat(IN_ZUSTELLUNG.erlaubteFolgestatus())
                .containsExactly(ZUGESTELLT, RETOURE, VERLOREN);
        assertThat(ZUGESTELLT.erlaubteFolgestatus()).isEmpty();
        assertThat(RETOURE.erlaubteFolgestatus()).isEmpty();
        assertThat(VERLOREN.erlaubteFolgestatus()).isEmpty();
        assertThat(paketIn(UEBERGEBEN).erlaubteFolgestatus())
                .containsExactly(IN_ZUSTELLUNG, ZUGESTELLT, RETOURE, VERLOREN);
    }

    @Test
    @DisplayName("US-07.2 AK7: fehlender Status ist ungültig")
    void statusFehlt() {
        assertThatThrownBy(() -> paket().aendereStatus(null, AM_13_11))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Versandpaket paket() {
        return new Versandpaket(Logistikdienstleister.DHL, "00340434161094042557", AM_12_11);
    }

    private static Versandpaket paketIn(Lieferstatus status) {
        Versandpaket paket = paket();
        if (status == RETOURE || status == VERLOREN) {
            paket.aendereStatus(UEBERGEBEN, AM_12_11);
        }
        if (status != ANGELEGT) {
            paket.aendereStatus(status, AM_12_11);
        }
        return paket;
    }
}
