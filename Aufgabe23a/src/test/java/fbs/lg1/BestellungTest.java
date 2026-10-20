package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

class BestellungTest {

    private Kunde kunde;
    private Produkt maus;
    private Produkt tastatur;
    private Date datum;
    private FakePaymentService paymentService;
    private Bestellung bestellung;

    @BeforeEach
    void prepare() {
        kunde = new Kunde(1, "Marcel Schachner", "schachner.marcel@icloud.com");
        Lieferant lieferant = new Lieferant(1, "Logitech");
        maus = new Produkt(1, "Maus", 10.0, 100, 5, lieferant);
        tastatur = new Produkt(2, "Tastatur", 5.5, 100, 5, lieferant);
        datum = new Date();
        paymentService = new FakePaymentService();
        bestellung = new Bestellung(1, datum, kunde, maus, 2, paymentService);
    }

    @Test
    void testInit() {
        assertThat(bestellung.showBestellnummer()).isEqualTo(1);
        assertThat(bestellung.showBestelldatum()).isSameAs(datum);
        assertThat(bestellung.showKunde()).isSameAs(kunde);
        assertThat(bestellung.showStatus()).isEqualTo(Status.OFFEN);
        assertThat(bestellung.showPositionen()).hasSize(1);
        assertThat(bestellung.showPositionen().get(0).showPositionsnummer()).isEqualTo(1);
        assertThat(bestellung.showPositionen().get(0).showMenge()).isEqualTo(2);
        assertThat(bestellung.showPositionen().get(0).showEinzelpreis()).isEqualTo(10.0);
        assertThat(bestellung.showPositionen().get(0).showProdukt()).isSameAs(maus);
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Bestellung(0, datum, kunde, maus, 1, paymentService))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bestellung(2, null, kunde, maus, 1, paymentService))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bestellung(2, datum, null, maus, 1, paymentService))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bestellung(2, datum, kunde, null, 1, paymentService))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bestellung(2, datum, kunde, maus, 0, paymentService))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bestellung(-1, datum, kunde, maus, 1, paymentService))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bestellung(2, datum, kunde, maus, -1, paymentService))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bestellung(2, datum, kunde, maus, 1, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testPositionHinzufuegen() {
        Bestellposition position = bestellung.positionHinzufuegen(tastatur, 1);

        assertThat(bestellung.showPositionen()).hasSize(2);
        assertThat(position.showPositionsnummer()).isEqualTo(2);
        assertThat(position.showProdukt()).isSameAs(tastatur);
    }

    @Test
    void testPositionHinzufuegenInvalid() {
        assertThatThrownBy(() -> bestellung.positionHinzufuegen(null, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bestellung.positionHinzufuegen(tastatur, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bestellung.positionHinzufuegen(tastatur, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testPositionHinzufuegenNichtOffen() {
        bestellung.versenden();

        assertThatThrownBy(() -> bestellung.positionHinzufuegen(tastatur, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testBerechneGesamtwert() {
        bestellung.positionHinzufuegen(tastatur, 1);

        assertThat(bestellung.berechneGesamtwert()).isEqualTo(25.5);
    }

    @Test
    void testBerechneGesamtwertHistorischerPreis() {
        maus.preisAendern(50.0);

        assertThat(bestellung.berechneGesamtwert()).isEqualTo(20.0);
        assertThat(bestellung.showPositionen().get(0).showEinzelpreis()).isEqualTo(10.0);
    }

    @Test
    void testStornieren() {
        boolean ergebnis = bestellung.stornieren();

        assertThat(ergebnis).isTrue();
        assertThat(bestellung.showStatus()).isEqualTo(Status.STORNIERT);
    }

    @Test
    void testStornierenBereitsStorniert() {
        bestellung.stornieren();

        boolean ergebnis = bestellung.stornieren();

        assertThat(ergebnis).isFalse();
        assertThat(bestellung.showStatus()).isEqualTo(Status.STORNIERT);
    }

    @Test
    void testStornierenVersendet() {
        bestellung.versenden();

        boolean ergebnis = bestellung.stornieren();

        assertThat(ergebnis).isFalse();
        assertThat(bestellung.showStatus()).isEqualTo(Status.VERSENDET);
    }

    @Test
    void testVersenden() {
        bestellung.versenden();

        assertThat(bestellung.showStatus()).isEqualTo(Status.VERSENDET);
    }

    @Test
    void testVersendenNichtOffen() {
        bestellung.stornieren();

        assertThatThrownBy(() -> bestellung.versenden()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testPositionHinzufuegenStorniert() {
        bestellung.stornieren();

        assertThatThrownBy(() -> bestellung.positionHinzufuegen(tastatur, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bestellung.showPositionen()).hasSize(1);
    }

    @Test
    void testBerechneGesamtwertRunden() {
        Lieferant lieferant = new Lieferant(2, "Cherry");
        Produkt kabel = new Produkt(3, "Kabel", 0.1, 100, 5, lieferant);
        Bestellung kabelbestellung = new Bestellung(2, datum, kunde, kabel, 3, paymentService);

        assertThat(kabelbestellung.berechneGesamtwert()).isEqualTo(0.3);
    }

    @Test
    void testStornierenRueckerstattung() {
        boolean ergebnis = bestellung.stornieren();

        assertThat(ergebnis).isTrue();
        assertThat(paymentService.showAnzahlStornierungen()).isEqualTo(1);
        assertThat(paymentService.showStornierterBetrag()).isEqualTo(20.0);
        assertThat(paymentService.showEmail()).isEqualTo("schachner.marcel@icloud.com");
    }

    @Test
    void testStornierenVersendetKeineRueckerstattung() {
        bestellung.versenden();

        bestellung.stornieren();

        assertThat(paymentService.showAnzahlStornierungen()).isEqualTo(0);
    }

    @Test
    void testVersendenBereitsVersendet() {
        bestellung.versenden();

        assertThatThrownBy(() -> bestellung.versenden()).isInstanceOf(IllegalStateException.class);
        assertThat(bestellung.showStatus()).isEqualTo(Status.VERSENDET);
    }
}
