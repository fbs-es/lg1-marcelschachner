package de.bestellsystem.kunde.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class KundeTest {

    private static final Anschrift BERLIN = new Anschrift("Hauptstraße 1", "Berlin", "10115", "DE");
    private static final Anschrift WIEN = new Anschrift("Ring 2", "Wien", "1010", "AT");

    private static Kunde anna() {
        return new Kunde("Anna", "Muster", new Email("anna@example.de"), BERLIN, false);
    }

    @Nested
    class Registrierung {

        @Test
        @DisplayName("US-01.1 AK1: Registrierung legt Kunde mit genau dieser Lieferadresse an")
        void erfolgreich() {
            Kunde kunde = anna();

            assertThat(kunde.getId()).isNotNull();
            assertThat(kunde.getVorname()).isEqualTo("Anna");
            assertThat(kunde.getNachname()).isEqualTo("Muster");
            assertThat(kunde.getEmail()).isEqualTo(new Email("anna@example.de"));
            assertThat(kunde.getAdressen())
                    .singleElement()
                    .satisfies(adresse -> {
                        assertThat(adresse.getId()).isNotNull();
                        assertThat(adresse.getAnschrift()).isEqualTo(BERLIN);
                        assertThat(adresse.istLieferadresse()).isTrue();
                        assertThat(adresse.istRechnungsadresse()).isFalse();
                    });
        }

        @Test
        @DisplayName("US-01.1 AK1: Kundenkennungen sind eindeutig")
        void neueKennung() {
            assertThat(anna().getId()).isNotEqualTo(anna().getId());
        }

        @Test
        @DisplayName("US-01.1 AK2: ohne Lieferadresse wird die Registrierung abgelehnt")
        void lieferadresseIstPflicht() {
            assertThatThrownBy(() ->
                            new Kunde("Anna", "Muster", new Email("anna@example.de"), null, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Lieferadresse");
        }

        @Test
        @DisplayName("US-01.1 AK5: Lieferadresse kann zugleich Rechnungsadresse sein")
        void auchRechnungsadresse() {
            Kunde kunde = new Kunde("Anna", "Muster", new Email("anna@example.de"), BERLIN, true);

            assertThat(kunde.getAdressen())
                    .singleElement()
                    .satisfies(adresse -> {
                        assertThat(adresse.istLieferadresse()).isTrue();
                        assertThat(adresse.istRechnungsadresse()).isTrue();
                    });
        }

        @Test
        @DisplayName("D-16: Namen sind Pflicht, getrimmt, höchstens 100 Zeichen")
        void namensregeln() {
            Email email = new Email("anna@example.de");
            Kunde kunde = new Kunde(" Anna ", " Muster ", email, BERLIN, false);
            assertThat(kunde.getVorname()).isEqualTo("Anna");
            assertThat(kunde.getNachname()).isEqualTo("Muster");

            assertThatThrownBy(() -> new Kunde(" ", "Muster", email, BERLIN, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("vorname");
            assertThatThrownBy(() -> new Kunde("Anna", null, email, BERLIN, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nachname");
            assertThatThrownBy(() -> new Kunde("A".repeat(101), "Muster", email, BERLIN, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("100");
            assertThatThrownBy(() -> new Kunde("Anna", "Muster", null, BERLIN, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email");
        }
    }

    @Nested
    class AdresseHinzufuegen {

        @Test
        @DisplayName("US-01.2 AK1: weitere Adresse mit neuer Kennung und genau diesen Kennzeichen")
        void hinzufuegen() {
            Kunde kunde = anna();

            Adresse neu = kunde.fuegeAdresseHinzu(WIEN, false, true);

            assertThat(kunde.getAdressen()).hasSize(2).last().isEqualTo(neu);
            assertThat(neu.getId()).isNotEqualTo(kunde.getAdressen().getFirst().getId());
            assertThat(neu.getAnschrift()).isEqualTo(WIEN);
            assertThat(neu.istLieferadresse()).isFalse();
            assertThat(neu.istRechnungsadresse()).isTrue();
        }

        @Test
        @DisplayName("US-01.2 AK2: Adresse ohne Kennzeichen wird abgelehnt (I-2)")
        void mindestensEinKennzeichen() {
            Kunde kunde = anna();

            assertThatThrownBy(() -> kunde.fuegeAdresseHinzu(WIEN, false, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("I-2");
            assertThat(kunde.getAdressen()).hasSize(1);
        }

        @Test
        @DisplayName("D-16: gleiche Anschrift darf mehrfach im Adressbuch stehen")
        void duplikateErlaubt() {
            Kunde kunde = anna();

            kunde.fuegeAdresseHinzu(BERLIN, true, false);

            assertThat(kunde.getAdressen()).hasSize(2);
        }
    }

    @Nested
    class AdresseAendern {

        @Test
        @DisplayName("US-01.3 AK1: Anschrift und Kennzeichen ändern, Kennung bleibt")
        void aendern() {
            Kunde kunde = anna();
            Adresse zweite = kunde.fuegeAdresseHinzu(WIEN, true, false);

            Adresse geaendert = kunde.aendereAdresse(zweite.getId(), BERLIN, false, true);

            assertThat(geaendert.getId()).isEqualTo(zweite.getId());
            assertThat(geaendert.getAnschrift()).isEqualTo(BERLIN);
            assertThat(geaendert.istLieferadresse()).isFalse();
            assertThat(geaendert.istRechnungsadresse()).isTrue();
        }

        @Test
        @DisplayName("US-01.3 AK2: letzte Lieferadresse behält ihr Kennzeichen (I-1)")
        void letzteLieferadresseBehaeltKennzeichen() {
            Kunde kunde = anna();
            Adresse einzige = kunde.getAdressen().getFirst();
            kunde.fuegeAdresseHinzu(WIEN, false, true);

            assertThatThrownBy(() -> kunde.aendereAdresse(einzige.getId(), WIEN, false, true))
                    .isInstanceOf(FachlicheAblehnungException.class)
                    .hasMessageContaining("Lieferadresse");
            assertThat(einzige.istLieferadresse()).isTrue();
            assertThat(einzige.getAnschrift()).isEqualTo(BERLIN);
        }

        @Test
        @DisplayName("US-01.3 AK2: Kennzeichen darf weg, wenn eine andere Lieferadresse bleibt")
        void kennzeichenEntziehenMitAndererLieferadresse() {
            Kunde kunde = anna();
            Adresse erste = kunde.getAdressen().getFirst();
            kunde.fuegeAdresseHinzu(WIEN, true, false);

            kunde.aendereAdresse(erste.getId(), BERLIN, false, true);

            assertThat(erste.istLieferadresse()).isFalse();
        }

        @Test
        @DisplayName("US-01.3 AK3: Adresse ohne Kennzeichen wird abgelehnt (I-2)")
        void mindestensEinKennzeichen() {
            Kunde kunde = anna();
            Adresse zweite = kunde.fuegeAdresseHinzu(WIEN, true, false);

            assertThatThrownBy(() -> kunde.aendereAdresse(zweite.getId(), WIEN, false, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("I-2");
            assertThat(zweite.istLieferadresse()).isTrue();
        }

        @Test
        @DisplayName("US-01.3 AK7: unbekannte Adresse ändern ergibt nicht gefunden")
        void unbekannt() {
            Kunde kunde = anna();

            assertThatThrownBy(() -> kunde.aendereAdresse(UUID.randomUUID(), WIEN, true, false))
                    .isInstanceOf(NichtGefundenException.class);
        }
    }

    @Nested
    class AdresseLoeschen {

        @Test
        @DisplayName("US-01.3 AK4: eine von zwei Lieferadressen löschen")
        void loeschen() {
            Kunde kunde = anna();
            Adresse erste = kunde.getAdressen().getFirst();
            Adresse zweite = kunde.fuegeAdresseHinzu(WIEN, true, false);

            kunde.entferneAdresse(erste.getId());

            assertThat(kunde.getAdressen()).containsExactly(zweite);
        }

        @Test
        @DisplayName("US-01.3 AK5: letzte Lieferadresse ist nicht löschbar (I-1)")
        void letzteLieferadresseNichtLoeschbar() {
            Kunde kunde = anna();
            Adresse einzige = kunde.getAdressen().getFirst();
            kunde.fuegeAdresseHinzu(WIEN, false, true);

            assertThatThrownBy(() -> kunde.entferneAdresse(einzige.getId()))
                    .isInstanceOf(FachlicheAblehnungException.class)
                    .hasMessageContaining("Lieferadresse");
            assertThat(kunde.getAdressen()).hasSize(2);
        }

        @Test
        @DisplayName("US-01.3 AK6: letzte Rechnungsadresse ist löschbar")
        void letzteRechnungsadresseLoeschbar() {
            Kunde kunde = anna();
            Adresse rechnung = kunde.fuegeAdresseHinzu(WIEN, false, true);

            kunde.entferneAdresse(rechnung.getId());

            assertThat(kunde.getAdressen()).hasSize(1);
        }

        @Test
        @DisplayName("US-01.3 AK7: unbekannte Adresse löschen ergibt nicht gefunden")
        void unbekannt() {
            Kunde kunde = anna();

            assertThatThrownBy(() -> kunde.entferneAdresse(UUID.randomUUID()))
                    .isInstanceOf(NichtGefundenException.class);
        }

        @Test
        void adressbuchIstVonAussenUnveraenderlich() {
            Kunde kunde = anna();

            assertThatThrownBy(() -> kunde.getAdressen().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class Lieferanschrift {

        @Test
        @DisplayName("US-04.1 AK5: Lieferanschrift ist eine Kopie der gewählten Lieferadresse")
        void kopie() {
            Kunde kunde = anna();
            UUID adressId = kunde.getAdressen().getFirst().getId();

            Anschrift snapshot = kunde.lieferanschrift(adressId);
            kunde.aendereAdresse(adressId, WIEN, true, false);

            assertThat(snapshot).isEqualTo(BERLIN);
        }

        @Test
        @DisplayName("US-04.1 AK8: fremde Adresse wird nicht gefunden")
        void fremdeAdresse() {
            UUID fremde = anna().getAdressen().getFirst().getId();

            assertThatThrownBy(() -> anna().lieferanschrift(fremde))
                    .isInstanceOf(NichtGefundenException.class);
        }

        @Test
        @DisplayName("US-04.1 AK8: Adresse ohne Lieferkennzeichen wird fachlich abgelehnt")
        void keineLieferadresse() {
            Kunde kunde = anna();
            UUID rechnungsadresse = kunde.fuegeAdresseHinzu(WIEN, false, true).getId();

            assertThatThrownBy(() -> kunde.lieferanschrift(rechnungsadresse))
                    .isInstanceOf(FachlicheAblehnungException.class)
                    .hasMessageContaining("Lieferadresse");
        }
    }
}
