package fbs.lg1;

import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BibliothekTest {

    private Bibliothek bibliothek;
    private Nutzer nutzer;
    private Date ausleihdatum;

    @BeforeEach
    void prepare() {
        bibliothek = new Bibliothek("Stadtbibliothek", "Hauptplatz 1, 1010 Wien");
        nutzer = new Nutzer(1, "Marcel Schachner", "schachner.marcel@icloud.com");
        ausleihdatum = new Date(1_000_000_000_000L);
    }

    @Test
    void testInit() {
        assertThat(bibliothek.showName()).isEqualTo("Stadtbibliothek");
        assertThat(bibliothek.showAdresse()).isEqualTo("Hauptplatz 1, 1010 Wien");
        assertThat(bibliothek.showBuecher()).isEmpty();
        assertThat(bibliothek.showNutzer()).isEmpty();
        assertThat(bibliothek.showAusleihen()).isEmpty();
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Bibliothek(null, "Hauptplatz 1, 1010 Wien"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bibliothek(" ", "Hauptplatz 1, 1010 Wien"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bibliothek("Stadtbibliothek", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bibliothek("Stadtbibliothek", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testBuchHinzufuegen() {
        Buch buch = bibliothek.buchHinzufuegen("978-3-16-148410-0", "Der Prozess", "Franz Kafka");

        assertThat(bibliothek.showBuecher()).hasSize(1);
        assertThat(bibliothek.showBuecher().get(0)).isSameAs(buch);
        assertThat(buch.showIsbn()).isEqualTo("978-3-16-148410-0");
        assertThat(buch.showTitel()).isEqualTo("Der Prozess");
        assertThat(buch.showAutor()).isEqualTo("Franz Kafka");
        assertThat(buch.istVerfuegbar()).isTrue();
    }

    @Test
    void testBuchHinzufuegenInvalid() {
        assertThatThrownBy(() -> bibliothek.buchHinzufuegen(null, "Der Prozess", "Franz Kafka"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bibliothek.buchHinzufuegen(" ", "Der Prozess", "Franz Kafka"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bibliothek.buchHinzufuegen("978-3-16-148410-0", null, "Franz Kafka"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bibliothek.buchHinzufuegen("978-3-16-148410-0", "Der Prozess", ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(bibliothek.showBuecher()).isEmpty();
    }

    @Test
    void testBuchHinzufuegenDoppelteIsbn() {
        bibliothek.buchHinzufuegen("978-3-16-148410-0", "Der Prozess", "Franz Kafka");

        assertThatThrownBy(() -> bibliothek.buchHinzufuegen("978-3-16-148410-0", "Das Schloss", "Franz Kafka"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bibliothek.showBuecher()).hasSize(1);
    }

    @Test
    void testNutzerRegistrieren() {
        Nutzer zweiterNutzer = new Nutzer(2, "Max Mustermann", "max.mustermann@gmail.com");

        bibliothek.nutzerRegistrieren(nutzer);
        bibliothek.nutzerRegistrieren(zweiterNutzer);

        assertThat(bibliothek.showNutzer()).hasSize(2);
        assertThat(bibliothek.showNutzer().get(0)).isSameAs(nutzer);
        assertThat(bibliothek.showNutzer().get(1)).isSameAs(zweiterNutzer);
    }

    @Test
    void testNutzerRegistrierenNull() {
        assertThatThrownBy(() -> bibliothek.nutzerRegistrieren(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(bibliothek.showNutzer()).isEmpty();
    }

    @Test
    void testNutzerRegistrierenDoppelteId() {
        Nutzer gleicheId = new Nutzer(1, "Max Mustermann", "max.mustermann@gmail.com");
        bibliothek.nutzerRegistrieren(nutzer);

        assertThatThrownBy(() -> bibliothek.nutzerRegistrieren(gleicheId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bibliothek.showNutzer()).hasSize(1);
    }

    @Test
    void testAusleiheStarten() {
        Buch buch = bibliothek.buchHinzufuegen("978-3-16-148410-0", "Der Prozess", "Franz Kafka");
        bibliothek.nutzerRegistrieren(nutzer);

        Ausleihe ausleihe = bibliothek.ausleiheStarten(nutzer, buch, ausleihdatum);

        assertThat(ausleihe.showNutzer()).isSameAs(nutzer);
        assertThat(ausleihe.showBuch()).isSameAs(buch);
        assertThat(ausleihe.showAusleihdatum()).isSameAs(ausleihdatum);
        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.AKTIV);
        assertThat(bibliothek.showAusleihen()).hasSize(1);
        assertThat(bibliothek.showAusleihen().get(0)).isSameAs(ausleihe);
        assertThat(buch.istVerfuegbar()).isFalse();
    }

    @Test
    void testAusleiheStartenNull() {
        Buch buch = bibliothek.buchHinzufuegen("978-3-16-148410-0", "Der Prozess", "Franz Kafka");
        bibliothek.nutzerRegistrieren(nutzer);

        assertThatThrownBy(() -> bibliothek.ausleiheStarten(null, buch, ausleihdatum))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bibliothek.ausleiheStarten(nutzer, null, ausleihdatum))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bibliothek.ausleiheStarten(nutzer, buch, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(bibliothek.showAusleihen()).isEmpty();
        assertThat(buch.istVerfuegbar()).isTrue();
    }

    @Test
    void testAusleiheStartenNutzerNichtRegistriert() {
        Buch buch = bibliothek.buchHinzufuegen("978-3-16-148410-0", "Der Prozess", "Franz Kafka");

        assertThatThrownBy(() -> bibliothek.ausleiheStarten(nutzer, buch, ausleihdatum))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bibliothek.showAusleihen()).isEmpty();
        assertThat(buch.istVerfuegbar()).isTrue();
    }

    @Test
    void testAusleiheStartenBuchNichtImBestand() {
        Buch fremdesBuch = new Buch("978-3-16-148410-0", "Der Prozess", "Franz Kafka");
        bibliothek.nutzerRegistrieren(nutzer);

        assertThatThrownBy(() -> bibliothek.ausleiheStarten(nutzer, fremdesBuch, ausleihdatum))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bibliothek.showAusleihen()).isEmpty();
        assertThat(fremdesBuch.istVerfuegbar()).isTrue();
    }

    @Test
    void testAusleiheStartenBuchNichtVerfuegbar() {
        Nutzer zweiterNutzer = new Nutzer(2, "Max Mustermann", "max.mustermann@gmail.com");
        Buch buch = bibliothek.buchHinzufuegen("978-3-16-148410-0", "Der Prozess", "Franz Kafka");
        bibliothek.nutzerRegistrieren(nutzer);
        bibliothek.nutzerRegistrieren(zweiterNutzer);
        bibliothek.ausleiheStarten(nutzer, buch, ausleihdatum);

        assertThatThrownBy(() -> bibliothek.ausleiheStarten(zweiterNutzer, buch, ausleihdatum))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bibliothek.showAusleihen()).hasSize(1);
    }

    @Test
    void testAusleiheStartenNachRueckgabe() {
        Nutzer zweiterNutzer = new Nutzer(2, "Max Mustermann", "max.mustermann@gmail.com");
        Buch buch = bibliothek.buchHinzufuegen("978-3-16-148410-0", "Der Prozess", "Franz Kafka");
        bibliothek.nutzerRegistrieren(nutzer);
        bibliothek.nutzerRegistrieren(zweiterNutzer);
        Ausleihe ersteAusleihe = bibliothek.ausleiheStarten(nutzer, buch, ausleihdatum);
        ersteAusleihe.zurueckgeben(ausleihdatum);

        Ausleihe zweiteAusleihe = bibliothek.ausleiheStarten(zweiterNutzer, buch, ausleihdatum);

        assertThat(zweiteAusleihe.showNutzer()).isSameAs(zweiterNutzer);
        assertThat(bibliothek.showAusleihen()).hasSize(2);
        assertThat(buch.istVerfuegbar()).isFalse();
    }
}
