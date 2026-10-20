package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RadiosendungTest {

    private Moderator moderator;
    private Radiosendung sendung;
    private Musikbeitrag musik;
    private Werbebeitrag werbung;
    private Moderation moderation;

    @BeforeEach
    void prepare() {
        moderator = new Moderator("Marcel Schachner");
        sendung =
                new Radiosendung(
                        "Morgenshow", LocalTime.of(8, 0), LocalTime.of(9, 0), 600, moderator);
        musik = new Musikbeitrag("Song", 200, "Künstler", "Album", "Pop");
        werbung = new Werbebeitrag("Spot", 100, "Firma", 500.0);
        moderation = new Moderation("Begrüßung", 60, "Guten Morgen!");
    }

    @Test
    void testInit() {
        assertThat(sendung.showName()).isEqualTo("Morgenshow");
        assertThat(sendung.showBeginn()).isEqualTo(LocalTime.of(8, 0));
        assertThat(sendung.showEnde()).isEqualTo(LocalTime.of(9, 0));
        assertThat(sendung.showMaxDauer()).isEqualTo(600);
        assertThat(sendung.showModerator()).isSameAs(moderator);
        assertThat(sendung.showBeitraege()).isEmpty();
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(0);
        assertThat(sendung.berechneRestzeit()).isEqualTo(600);
    }

    @Test
    void testInitInvalid() {
        LocalTime acht = LocalTime.of(8, 0);
        LocalTime neun = LocalTime.of(9, 0);

        assertThatThrownBy(() -> new Radiosendung("", acht, neun, 600, moderator))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosendung("Show", null, neun, 600, moderator))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosendung("Show", acht, null, 600, moderator))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosendung("Show", neun, acht, 600, moderator))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosendung("Show", acht, neun, 0, moderator))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosendung("Show", acht, neun, 3601, moderator))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosendung("Show", acht, neun, 600, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testBeitragHinzufuegen() {
        boolean ergebnis = sendung.beitragHinzufuegen(musik);
        sendung.beitragHinzufuegen(werbung);

        assertThat(ergebnis).isTrue();
        assertThat(sendung.showBeitraege()).hasSize(2);
        assertThat(musik.showStartzeit()).isEqualTo(0);
        assertThat(werbung.showStartzeit()).isEqualTo(200);
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(300);
        assertThat(sendung.berechneRestzeit()).isEqualTo(300);
    }

    @Test
    void testBeitragHinzufuegenZuLang() {
        Musikbeitrag langerSong = new Musikbeitrag("Langer Song", 601, "Künstler", "Album", "Rock");

        boolean ergebnis = sendung.beitragHinzufuegen(langerSong);

        assertThat(ergebnis).isFalse();
        assertThat(sendung.showBeitraege()).isEmpty();
    }

    @Test
    void testBeitragHinzufuegenGenauMaxDauer() {
        Musikbeitrag song = new Musikbeitrag("Song", 600, "Künstler", "Album", "Rock");

        boolean ergebnis = sendung.beitragHinzufuegen(song);

        assertThat(ergebnis).isTrue();
        assertThat(sendung.berechneRestzeit()).isEqualTo(0);
    }

    @Test
    void testBeitragHinzufuegenInvalid() {
        sendung.beitragHinzufuegen(musik);

        assertThatThrownBy(() -> sendung.beitragHinzufuegen(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.beitragHinzufuegen(musik))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testBeitragEntfernen() {
        sendung.beitragHinzufuegen(musik);
        sendung.beitragHinzufuegen(werbung);
        sendung.beitragHinzufuegen(moderation);

        sendung.beitragEntfernen(musik);

        assertThat(sendung.showBeitraege()).hasSize(2);
        assertThat(werbung.showStartzeit()).isEqualTo(0);
        assertThat(moderation.showStartzeit()).isEqualTo(100);
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(160);
    }

    @Test
    void testBeitragEntfernenNichtVorhanden() {
        assertThatThrownBy(() -> sendung.beitragEntfernen(musik))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testBeitragKuerzen() {
        sendung.beitragHinzufuegen(musik);
        sendung.beitragHinzufuegen(werbung);

        sendung.beitragKuerzen(musik, 150);

        assertThat(musik.showDauer()).isEqualTo(150);
        assertThat(werbung.showStartzeit()).isEqualTo(150);
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(250);
    }

    @Test
    void testBeitragKuerzenInvalid() {
        sendung.beitragHinzufuegen(musik);

        assertThatThrownBy(() -> sendung.beitragKuerzen(musik, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.beitragKuerzen(musik, 200))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.beitragKuerzen(werbung, 50))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testBeitragVerschieben() {
        sendung.beitragHinzufuegen(musik);
        sendung.beitragHinzufuegen(werbung);
        sendung.beitragHinzufuegen(moderation);

        sendung.beitragVerschieben(moderation, 0);

        assertThat(sendung.showBeitraege().get(0)).isSameAs(moderation);
        assertThat(moderation.showStartzeit()).isEqualTo(0);
        assertThat(musik.showStartzeit()).isEqualTo(60);
        assertThat(werbung.showStartzeit()).isEqualTo(260);
    }

    @Test
    void testBeitragVerschiebenInvalid() {
        sendung.beitragHinzufuegen(musik);

        assertThatThrownBy(() -> sendung.beitragVerschieben(musik, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.beitragVerschieben(musik, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.beitragVerschieben(werbung, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testEilmeldungEinfuegenAmBeitragsbeginn() {
        sendung.beitragHinzufuegen(musik);
        sendung.beitragHinzufuegen(werbung);
        Eilmeldung eilmeldung = new Eilmeldung("Unwetter", 30, 4, "Kärnten");

        sendung.eilmeldungEinfuegen(eilmeldung, 200);

        assertThat(sendung.showBeitraege()).hasSize(3);
        assertThat(sendung.showBeitraege().get(1)).isSameAs(eilmeldung);
        assertThat(eilmeldung.showStartzeit()).isEqualTo(200);
        assertThat(werbung.showStartzeit()).isEqualTo(230);
    }

    @Test
    void testEilmeldungEinfuegenSplittet() {
        sendung.beitragHinzufuegen(musik);
        sendung.beitragHinzufuegen(werbung);
        Eilmeldung eilmeldung = new Eilmeldung("Unwetter", 30, 4, "Kärnten");

        sendung.eilmeldungEinfuegen(eilmeldung, 80);

        assertThat(sendung.showBeitraege()).hasSize(4);
        assertThat(sendung.showBeitraege().get(0)).isSameAs(musik);
        assertThat(musik.showDauer()).isEqualTo(80);
        assertThat(sendung.showBeitraege().get(1)).isSameAs(eilmeldung);
        assertThat(eilmeldung.showStartzeit()).isEqualTo(80);
        Radiobeitrag rest = sendung.showBeitraege().get(2);
        assertThat(rest).isInstanceOf(Musikbeitrag.class);
        assertThat(rest.showTitel()).isEqualTo("Song");
        assertThat(rest.showDauer()).isEqualTo(120);
        assertThat(rest.showStartzeit()).isEqualTo(110);
        assertThat(werbung.showStartzeit()).isEqualTo(230);
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(330);
    }

    @Test
    void testEilmeldungEinfuegenAmEnde() {
        sendung.beitragHinzufuegen(musik);
        Eilmeldung eilmeldung = new Eilmeldung("Unwetter", 30, 4, "Kärnten");

        sendung.eilmeldungEinfuegen(eilmeldung, 500);

        assertThat(sendung.showBeitraege().get(1)).isSameAs(eilmeldung);
        assertThat(eilmeldung.showStartzeit()).isEqualTo(200);
    }

    @Test
    void testEilmeldungEinfuegenUeberschreitetZeitlimit() {
        Musikbeitrag song = new Musikbeitrag("Song", 600, "Künstler", "Album", "Rock");
        sendung.beitragHinzufuegen(song);
        Eilmeldung eilmeldung = new Eilmeldung("Unwetter", 30, 5, "Kärnten");

        sendung.eilmeldungEinfuegen(eilmeldung, 300);

        assertThat(sendung.showBeitraege()).hasSize(3);
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(630);
        assertThat(sendung.berechneRestzeit()).isEqualTo(-30);
    }

    @Test
    void testEilmeldungEinfuegenInvalid() {
        Eilmeldung eilmeldung = new Eilmeldung("Unwetter", 30, 4, "Kärnten");
        sendung.eilmeldungEinfuegen(eilmeldung, 0);

        assertThatThrownBy(() -> sendung.eilmeldungEinfuegen(null, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.eilmeldungEinfuegen(eilmeldung, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.eilmeldungEinfuegen(eilmeldung, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testShowBeitraegeNichtVeraenderbar() {
        sendung.beitragHinzufuegen(musik);

        assertThatThrownBy(() -> sendung.showBeitraege().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(sendung.showBeitraege()).hasSize(1);
    }
}
