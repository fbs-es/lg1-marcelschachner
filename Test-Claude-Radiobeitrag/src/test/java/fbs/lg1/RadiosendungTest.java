package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RadiosendungTest {

    private Radiosendung sendung;
    private Moderation moderation;
    private Musikbeitrag musik;
    private Werbebeitrag werbung;

    @BeforeEach
    void prepare() {
        sendung =
                new Radiosendung(
                        "Morgenshow", "Marcel Schachner", LocalTime.of(8, 0), LocalTime.of(9, 0), 900);
        moderation = new Moderation("Begrüßung", 60, "Guten Morgen!");
        musik = new Musikbeitrag("Wolkenlos", 240, "Die Wolken", "Himmelblau", Musikgenre.POP);
        werbung = new Werbebeitrag("Kaffee-Spot", 30, "Bohnenglück GmbH", 5000.0);
    }

    @Test
    void testInit() {
        assertThat(sendung.showName()).isEqualTo("Morgenshow");
        assertThat(sendung.showModerator()).isEqualTo("Marcel Schachner");
        assertThat(sendung.showStartzeit()).isEqualTo(LocalTime.of(8, 0));
        assertThat(sendung.showEndzeit()).isEqualTo(LocalTime.of(9, 0));
        assertThat(sendung.showMaximaldauer()).isEqualTo(900);
        assertThat(sendung.showBeitraege()).isEmpty();
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(0);
        assertThat(sendung.berechneRestzeit()).isEqualTo(900);
        assertThat(sendung.istZeitlimitUeberschritten()).isFalse();
    }

    @Test
    void testInitInvalid() {
        LocalTime acht = LocalTime.of(8, 0);
        LocalTime neun = LocalTime.of(9, 0);
        assertThatThrownBy(() -> new Radiosendung(null, "Marcel Schachner", acht, neun, 900))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosendung("Morgenshow", " ", acht, neun, 900))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosendung("Morgenshow", "Marcel Schachner", null, neun, 900))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosendung("Morgenshow", "Marcel Schachner", acht, null, 900))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosendung("Morgenshow", "Marcel Schachner", neun, acht, 900))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosendung("Morgenshow", "Marcel Schachner", acht, neun, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosendung("Morgenshow", "Marcel Schachner", acht, neun, 3601))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testBeitragHinzufuegen() {
        sendung.beitragHinzufuegen(moderation);
        sendung.beitragHinzufuegen(musik);
        sendung.beitragHinzufuegen(werbung);

        assertThat(sendung.showBeitraege()).hasSize(3);
        assertThat(sendung.showBeitraege().get(1)).isSameAs(musik);
        assertThat(moderation.showStartzeitpunkt()).isEqualTo(0);
        assertThat(musik.showStartzeitpunkt()).isEqualTo(60);
        assertThat(werbung.showStartzeitpunkt()).isEqualTo(300);
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(330);
        assertThat(sendung.berechneRestzeit()).isEqualTo(570);
    }

    @Test
    void testBeitragHinzufuegenGenauMaximaldauer() {
        Musikbeitrag lang = new Musikbeitrag("Lang", 900, "Die Wolken", "Himmelblau", Musikgenre.ROCK);

        sendung.beitragHinzufuegen(lang);

        assertThat(sendung.berechneRestzeit()).isEqualTo(0);
        assertThat(sendung.istZeitlimitUeberschritten()).isFalse();
    }

    @Test
    void testBeitragHinzufuegenZuLang() {
        sendung.beitragHinzufuegen(musik);
        Musikbeitrag lang = new Musikbeitrag("Lang", 661, "Die Wolken", "Himmelblau", Musikgenre.ROCK);

        assertThatThrownBy(() -> sendung.beitragHinzufuegen(lang))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sendung.showBeitraege()).hasSize(1);
    }

    @Test
    void testBeitragHinzufuegenNull() {
        assertThatThrownBy(() -> sendung.beitragHinzufuegen(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testBeitragHinzufuegenDoppelt() {
        sendung.beitragHinzufuegen(musik);

        assertThatThrownBy(() -> sendung.beitragHinzufuegen(musik))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testBeitragWiederherstellen() {
        Musikbeitrag lang = new Musikbeitrag("Lang", 900, "Die Wolken", "Himmelblau", Musikgenre.ROCK);

        sendung.beitragWiederherstellen(moderation);
        sendung.beitragWiederherstellen(lang);

        assertThat(sendung.showBeitraege()).hasSize(2);
        assertThat(sendung.showBeitraege().get(1)).isSameAs(lang);
        assertThat(lang.showStartzeitpunkt()).isEqualTo(60);
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(960);
        assertThat(sendung.istZeitlimitUeberschritten()).isTrue();
    }

    @Test
    void testBeitragWiederherstellenInvalid() {
        sendung.beitragWiederherstellen(musik);

        assertThatThrownBy(() -> sendung.beitragWiederherstellen(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.beitragWiederherstellen(musik))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testBeitragKuerzen() {
        sendung.beitragHinzufuegen(moderation);
        sendung.beitragHinzufuegen(musik);
        sendung.beitragHinzufuegen(werbung);

        sendung.beitragKuerzen(moderation, 20);

        assertThat(moderation.showDauer()).isEqualTo(20);
        assertThat(musik.showStartzeitpunkt()).isEqualTo(20);
        assertThat(werbung.showStartzeitpunkt()).isEqualTo(260);
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(290);
    }

    @Test
    void testBeitragKuerzenInvalid() {
        sendung.beitragHinzufuegen(musik);

        assertThatThrownBy(() -> sendung.beitragKuerzen(null, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.beitragKuerzen(musik, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.beitragKuerzen(musik, 240))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.beitragKuerzen(werbung, 10))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testBeitragLoeschen() {
        sendung.beitragHinzufuegen(moderation);
        sendung.beitragHinzufuegen(musik);
        sendung.beitragHinzufuegen(werbung);

        sendung.beitragLoeschen(moderation);

        assertThat(sendung.showBeitraege()).hasSize(2);
        assertThat(musik.showStartzeitpunkt()).isEqualTo(0);
        assertThat(werbung.showStartzeitpunkt()).isEqualTo(240);
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(270);
    }

    @Test
    void testBeitragLoeschenInvalid() {
        assertThatThrownBy(() -> sendung.beitragLoeschen(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.beitragLoeschen(musik))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testBeitragVerschieben() {
        sendung.beitragHinzufuegen(moderation);
        sendung.beitragHinzufuegen(musik);
        sendung.beitragHinzufuegen(werbung);

        sendung.beitragVerschieben(werbung, 0);

        assertThat(sendung.showBeitraege().get(0)).isSameAs(werbung);
        assertThat(werbung.showStartzeitpunkt()).isEqualTo(0);
        assertThat(moderation.showStartzeitpunkt()).isEqualTo(30);
        assertThat(musik.showStartzeitpunkt()).isEqualTo(90);
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(330);
    }

    @Test
    void testBeitragVerschiebenInvalid() {
        sendung.beitragHinzufuegen(musik);

        assertThatThrownBy(() -> sendung.beitragVerschieben(null, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.beitragVerschieben(musik, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.beitragVerschieben(musik, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.beitragVerschieben(werbung, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testEilmeldungEinfuegenVorBeitrag() {
        sendung.beitragHinzufuegen(moderation);
        sendung.beitragHinzufuegen(musik);
        Eilmeldung eilmeldung = new Eilmeldung("Sturmwarnung", 30, 4, "Kärnten");

        sendung.eilmeldungEinfuegen(eilmeldung, 60);

        assertThat(sendung.showBeitraege()).hasSize(3);
        assertThat(sendung.showBeitraege().get(1)).isSameAs(eilmeldung);
        assertThat(eilmeldung.showStartzeitpunkt()).isEqualTo(60);
        assertThat(musik.showStartzeitpunkt()).isEqualTo(90);
        assertThat(musik.showDauer()).isEqualTo(240);
        assertThat(sendung.istZeitlimitUeberschritten()).isFalse();
    }

    @Test
    void testEilmeldungEinfuegenSplitting() {
        sendung.beitragHinzufuegen(moderation);
        sendung.beitragHinzufuegen(musik);
        sendung.beitragHinzufuegen(werbung);
        Eilmeldung eilmeldung = new Eilmeldung("Sturmwarnung", 30, 4, "Kärnten");

        sendung.eilmeldungEinfuegen(eilmeldung, 100);

        assertThat(sendung.showBeitraege()).hasSize(5);
        assertThat(sendung.showBeitraege().get(1)).isSameAs(musik);
        assertThat(musik.showDauer()).isEqualTo(40);
        assertThat(sendung.showBeitraege().get(2)).isSameAs(eilmeldung);
        assertThat(eilmeldung.showStartzeitpunkt()).isEqualTo(100);
        Radiobeitrag fortsetzung = sendung.showBeitraege().get(3);
        assertThat(fortsetzung).isInstanceOf(Musikbeitrag.class);
        assertThat(fortsetzung.showTitel()).isEqualTo("Wolkenlos (Fortsetzung)");
        assertThat(fortsetzung.showDauer()).isEqualTo(200);
        assertThat(fortsetzung.showStartzeitpunkt()).isEqualTo(130);
        assertThat(((Musikbeitrag) fortsetzung).showKuenstler()).isEqualTo("Die Wolken");
        assertThat(sendung.showBeitraege().get(4)).isSameAs(werbung);
        assertThat(werbung.showStartzeitpunkt()).isEqualTo(330);
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(360);
    }

    @Test
    void testEilmeldungEinfuegenAmEnde() {
        sendung.beitragHinzufuegen(musik);
        Eilmeldung eilmeldung = new Eilmeldung("Sturmwarnung", 30, 4, "Kärnten");

        sendung.eilmeldungEinfuegen(eilmeldung, 500);

        assertThat(sendung.showBeitraege().get(1)).isSameAs(eilmeldung);
        assertThat(eilmeldung.showStartzeitpunkt()).isEqualTo(240);
    }

    @Test
    void testEilmeldungEinfuegenZeitlimitUeberschritten() {
        Musikbeitrag lang = new Musikbeitrag("Lang", 900, "Die Wolken", "Himmelblau", Musikgenre.ROCK);
        sendung.beitragHinzufuegen(lang);
        Eilmeldung eilmeldung = new Eilmeldung("Sturmwarnung", 120, 5, "Kärnten");

        sendung.eilmeldungEinfuegen(eilmeldung, 300);

        assertThat(sendung.istZeitlimitUeberschritten()).isTrue();
        assertThat(sendung.berechneGesamtdauer()).isEqualTo(1020);
        assertThat(sendung.berechneRestzeit()).isEqualTo(-120);
        assertThat(sendung.showBeitraege()).hasSize(3);
    }

    @Test
    void testEilmeldungEinfuegenInvalid() {
        Eilmeldung eilmeldung = new Eilmeldung("Sturmwarnung", 30, 4, "Kärnten");
        sendung.eilmeldungEinfuegen(eilmeldung, 0);

        assertThatThrownBy(() -> sendung.eilmeldungEinfuegen(null, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.eilmeldungEinfuegen(eilmeldung, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendung.eilmeldungEinfuegen(eilmeldung, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
