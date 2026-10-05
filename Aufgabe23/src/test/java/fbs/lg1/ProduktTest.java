package fbs.lg1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProduktTest {

    private Lieferant lieferant;
    private Produkt produkt;

    @BeforeEach
    void prepare() {
        lieferant = new Lieferant(1, "Logitech");
        produkt = new Produkt(1, "Maus", 10.0, 10, 3, lieferant);
    }

    @Test
    void testInit() {
        assertThat(produkt.showProduktnr()).isEqualTo(1);
        assertThat(produkt.showBezeichnung()).isEqualTo("Maus");
        assertThat(produkt.showEinzelpreis()).isEqualTo(10.0);
        assertThat(produkt.showLagerbestand()).isEqualTo(10);
        assertThat(produkt.showMindestbestand()).isEqualTo(3);
        assertThat(produkt.showLieferant()).isSameAs(lieferant);
        assertThat(produkt.showLieferantenbestellungen()).isEmpty();
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Produkt(0, "Maus", 10.0, 10, 3, lieferant))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt(1, null, 10.0, 10, 3, lieferant))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt(1, " ", 10.0, 10, 3, lieferant))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt(1, "Maus", 0.0, 10, 3, lieferant))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt(1, "Maus", 10.0, -1, 3, lieferant))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt(1, "Maus", 10.0, 10, 0, lieferant))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt(-1, "Maus", 10.0, 10, 3, lieferant))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt(1, "Maus", -5.0, 10, 3, lieferant))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt(1, "Maus", 10.0, 10, -1, lieferant))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt(1, "Maus", 10.0, 10, 3, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testPruefeNachschubbedarfKeinBedarf() {
        boolean ergebnis = produkt.pruefeNachschubbedarf();

        assertThat(ergebnis).isFalse();
        assertThat(produkt.showLieferantenbestellungen()).isEmpty();
    }

    @Test
    void testPruefeNachschubbedarfMindestbestandErreicht() {
        Produkt knapp = new Produkt(2, "Tastatur", 20.0, 3, 3, lieferant);

        boolean ergebnis = knapp.pruefeNachschubbedarf();

        assertThat(ergebnis).isTrue();
        assertThat(knapp.showLieferantenbestellungen()).hasSize(1);
        Lieferantenbestellung lieferantenbestellung = knapp.showLieferantenbestellungen().get(0);
        assertThat(lieferantenbestellung.showMenge()).isEqualTo(3);
        assertThat(lieferantenbestellung.showProdukt()).isSameAs(knapp);
        assertThat(lieferantenbestellung.showLieferant()).isSameAs(lieferant);
        assertThat(lieferantenbestellung.showLieferantenbestellnummer()).isEqualTo(1);
        assertThat(lieferantenbestellung.showDatum()).isNotNull();
    }

    @Test
    void testPruefeNachschubbedarfUnterschritten() {
        Produkt leer = new Produkt(2, "Tastatur", 20.0, 0, 3, lieferant);

        boolean ergebnis = leer.pruefeNachschubbedarf();

        assertThat(ergebnis).isTrue();
        assertThat(leer.showLieferantenbestellungen().get(0).showMenge()).isEqualTo(6);
    }

    @Test
    void testPreisAendern() {
        produkt.preisAendern(12.5);

        assertThat(produkt.showEinzelpreis()).isEqualTo(12.5);
    }

    @Test
    void testPreisAendernInvalid() {
        assertThatThrownBy(() -> produkt.preisAendern(0.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> produkt.preisAendern(-1.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(produkt.showEinzelpreis()).isEqualTo(10.0);
    }

    @Test
    void testPruefeNachschubbedarfLueckenlos() {
        Produkt leer = new Produkt(2, "Tastatur", 20.0, 0, 3, lieferant);

        leer.pruefeNachschubbedarf();
        leer.pruefeNachschubbedarf();

        assertThat(leer.showLieferantenbestellungen()).hasSize(2);
        assertThat(leer.showLieferantenbestellungen().get(0).showLieferantenbestellnummer())
                .isEqualTo(1);
        assertThat(leer.showLieferantenbestellungen().get(1).showLieferantenbestellnummer())
                .isEqualTo(2);
    }

    @Test
    void testPruefeNachschubbedarfKnappUeberMindestbestand() {
        Produkt knapp = new Produkt(2, "Tastatur", 20.0, 4, 3, lieferant);

        boolean ergebnis = knapp.pruefeNachschubbedarf();

        assertThat(ergebnis).isFalse();
        assertThat(knapp.showLieferantenbestellungen()).isEmpty();
    }
}
