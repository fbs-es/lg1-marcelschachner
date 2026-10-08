package fbs.lg1;

import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AusleiheTest {

    private static final long TAG = 24L * 60 * 60 * 1000;

    private Nutzer nutzer;
    private Buch buch;
    private Date ausleihdatum;
    private Ausleihe ausleihe;

    @BeforeEach
    void prepare() {
        nutzer = new Nutzer(1, "Marcel Schachner", "schachner.marcel@icloud.com");
        buch = new Buch("978-3-16-148410-0", "Der Prozess", "Franz Kafka");
        ausleihdatum = new Date(1_000_000_000_000L);
        ausleihe = new Ausleihe(ausleihdatum, nutzer, buch);
    }

    @Test
    void testInit() {
        assertThat(ausleihe.showAusleihdatum()).isSameAs(ausleihdatum);
        assertThat(ausleihe.showRueckgabedatum()).isEqualTo(tageNach(14));
        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.AKTIV);
        assertThat(ausleihe.istVerlaengert()).isFalse();
        assertThat(ausleihe.showStrafe()).isEqualTo(0.0);
        assertThat(ausleihe.showBuch()).isSameAs(buch);
        assertThat(ausleihe.showNutzer()).isSameAs(nutzer);
        assertThat(buch.istVerfuegbar()).isFalse();
    }

    @Test
    void testInitInvalid() {
        Buch anderesBuch = new Buch("978-0-00-000000-1", "Das Schloss", "Franz Kafka");

        assertThatThrownBy(() -> new Ausleihe(null, nutzer, anderesBuch))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Ausleihe(ausleihdatum, null, anderesBuch))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Ausleihe(ausleihdatum, nutzer, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(anderesBuch.istVerfuegbar()).isTrue();
    }

    @Test
    void testInitBuchNichtVerfuegbar() {
        assertThatThrownBy(() -> new Ausleihe(ausleihdatum, nutzer, buch))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testVerlaengern() {
        ausleihe.verlaengern();

        assertThat(ausleihe.istVerlaengert()).isTrue();
        assertThat(ausleihe.showRueckgabedatum()).isEqualTo(tageNach(28));
        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.AKTIV);
    }

    @Test
    void testVerlaengernZweimal() {
        ausleihe.verlaengern();

        assertThatThrownBy(() -> ausleihe.verlaengern()).isInstanceOf(IllegalStateException.class);
        assertThat(ausleihe.showRueckgabedatum()).isEqualTo(tageNach(28));
    }

    @Test
    void testVerlaengernNachRueckgabe() {
        ausleihe.zurueckgeben(tageNach(5));

        assertThatThrownBy(() -> ausleihe.verlaengern()).isInstanceOf(IllegalStateException.class);
        assertThat(ausleihe.istVerlaengert()).isFalse();
    }

    @Test
    void testZurueckgebenPuenktlich() {
        ausleihe.zurueckgeben(tageNach(10));

        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.ZURUECKGEGEBEN);
        assertThat(ausleihe.showStrafe()).isEqualTo(0.0);
        assertThat(buch.istVerfuegbar()).isTrue();
    }

    @Test
    void testZurueckgebenAmRueckgabedatum() {
        ausleihe.zurueckgeben(tageNach(14));

        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.ZURUECKGEGEBEN);
        assertThat(ausleihe.showStrafe()).isEqualTo(0.0);
    }

    @Test
    void testZurueckgebenAmAusleihdatum() {
        ausleihe.zurueckgeben(ausleihdatum);

        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.ZURUECKGEGEBEN);
        assertThat(ausleihe.showStrafe()).isEqualTo(0.0);
    }

    @Test
    void testZurueckgebenVerspaetet() {
        ausleihe.zurueckgeben(tageNach(17));

        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.ZURUECKGEGEBEN);
        assertThat(ausleihe.showStrafe()).isEqualTo(1.5);
        assertThat(buch.istVerfuegbar()).isTrue();
    }

    @Test
    void testZurueckgebenAngefangenerTag() {
        Date datum = new Date(tageNach(14).getTime() + 1);

        ausleihe.zurueckgeben(datum);

        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.ZURUECKGEGEBEN);
        assertThat(ausleihe.showStrafe()).isEqualTo(0.5);
    }

    @Test
    void testZurueckgebenVerlaengertKeineStrafe() {
        ausleihe.verlaengern();

        ausleihe.zurueckgeben(tageNach(20));

        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.ZURUECKGEGEBEN);
        assertThat(ausleihe.showStrafe()).isEqualTo(0.0);
    }

    @Test
    void testZurueckgebenVerlaengertVerspaetet() {
        ausleihe.verlaengern();

        ausleihe.zurueckgeben(tageNach(30));

        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.ZURUECKGEGEBEN);
        assertThat(ausleihe.showStrafe()).isEqualTo(1.0);
    }

    @Test
    void testZurueckgebenInvalid() {
        Date vorAusleihe = new Date(ausleihdatum.getTime() - TAG);

        assertThatThrownBy(() -> ausleihe.zurueckgeben(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ausleihe.zurueckgeben(vorAusleihe))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.AKTIV);
        assertThat(buch.istVerfuegbar()).isFalse();
    }

    @Test
    void testZurueckgebenZweimal() {
        ausleihe.zurueckgeben(tageNach(17));

        assertThatThrownBy(() -> ausleihe.zurueckgeben(tageNach(20)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ausleihe.showStrafe()).isEqualTo(1.5);
        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.ZURUECKGEGEBEN);
    }

    @Test
    void testBuchNachRueckgabeErneutAusleihbar() {
        ausleihe.zurueckgeben(tageNach(10));

        Ausleihe neueAusleihe = new Ausleihe(tageNach(11), nutzer, buch);

        assertThat(neueAusleihe.showStatus()).isEqualTo(AusleihStatus.AKTIV);
        assertThat(buch.istVerfuegbar()).isFalse();
    }

    @Test
    void testPruefeUeberzogenInFrist() {
        boolean ergebnis = ausleihe.pruefeUeberzogen(tageNach(14));

        assertThat(ergebnis).isFalse();
        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.AKTIV);
    }

    @Test
    void testPruefeUeberzogen() {
        boolean ergebnis = ausleihe.pruefeUeberzogen(tageNach(15));

        assertThat(ergebnis).isTrue();
        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.UEBERZOGEN);
        assertThat(ausleihe.showStrafe()).isEqualTo(0.0);
        assertThat(buch.istVerfuegbar()).isFalse();
    }

    @Test
    void testPruefeUeberzogenVerlaengert() {
        ausleihe.verlaengern();

        boolean ergebnis = ausleihe.pruefeUeberzogen(tageNach(20));

        assertThat(ergebnis).isFalse();
        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.AKTIV);
    }

    @Test
    void testPruefeUeberzogenBleibtUeberzogen() {
        ausleihe.pruefeUeberzogen(tageNach(15));

        boolean ergebnis = ausleihe.pruefeUeberzogen(tageNach(16));

        assertThat(ergebnis).isTrue();
        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.UEBERZOGEN);
    }

    @Test
    void testPruefeUeberzogenNachRueckgabe() {
        ausleihe.zurueckgeben(tageNach(17));

        boolean ergebnis = ausleihe.pruefeUeberzogen(tageNach(20));

        assertThat(ergebnis).isFalse();
        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.ZURUECKGEGEBEN);
    }

    @Test
    void testPruefeUeberzogenNull() {
        assertThatThrownBy(() -> ausleihe.pruefeUeberzogen(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.AKTIV);
    }

    @Test
    void testVerlaengernUeberzogen() {
        ausleihe.pruefeUeberzogen(tageNach(15));

        assertThatThrownBy(() -> ausleihe.verlaengern()).isInstanceOf(IllegalStateException.class);
        assertThat(ausleihe.istVerlaengert()).isFalse();
        assertThat(ausleihe.showRueckgabedatum()).isEqualTo(tageNach(14));
    }

    @Test
    void testZurueckgebenUeberzogen() {
        ausleihe.pruefeUeberzogen(tageNach(15));

        ausleihe.zurueckgeben(tageNach(16));

        assertThat(ausleihe.showStatus()).isEqualTo(AusleihStatus.ZURUECKGEGEBEN);
        assertThat(ausleihe.showStrafe()).isEqualTo(1.0);
        assertThat(buch.istVerfuegbar()).isTrue();
    }

    private Date tageNach(int tage) {
        long millis = ausleihdatum.getTime() + tage * TAG;
        return new Date(millis);
    }
}
