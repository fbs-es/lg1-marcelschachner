package de.bestellsystem.mahnwesen.application;

import de.bestellsystem.gemeinsam.application.Benachrichtigung;
import de.bestellsystem.mahnwesen.domain.Mahnbenachrichtigung;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/** Formuliert die E-Mail zu einer Mahnbenachrichtigung (D-03). */
final class Mahnschreiben {

    static final String BETREFF = "Zahlungserinnerung";

    private static final DateTimeFormatter DATUM = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private Mahnschreiben() {}

    static Benachrichtigung von(Mahnbenachrichtigung mahnung) {
        String zeilen =
                mahnung.positionen().stream()
                        .map(position ->
                                "- %s, fällig am %s: offen %s"
                                        .formatted(
                                                position.bestellnummer(),
                                                DATUM.format(position.faelligAm()),
                                                position.offenerBetrag()))
                        .collect(Collectors.joining("\n"));
        String text =
                """
                Guten Tag,

                zu folgenden Bestellungen ist die Zahlung überfällig (Stand %s):

                %s

                Summe offen: %s

                Bitte begleichen Sie den offenen Betrag. Haben Sie bereits bezahlt, ist diese \
                Nachricht gegenstandslos.
                """
                        .formatted(DATUM.format(mahnung.stichtag()), zeilen, mahnung.summe());
        return new Benachrichtigung(mahnung.empfaenger(), BETREFF, text);
    }
}
