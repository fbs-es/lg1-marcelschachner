package de.bestellsystem.gemeinsam.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import de.bestellsystem.FesteUhrKonfiguration;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.gemeinsam.application.BestellnummernGenerator;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import({TestcontainersKonfiguration.class, FesteUhrKonfiguration.class})
class SequenzBestellnummernZaehlerIT {

    private static final LocalDate BESTELLDATUM = LocalDate.of(2026, 3, 15);

    @Autowired private BestellnummernGenerator generator;

    @Test
    @DisplayName("D-13/D-14: Bestellnummern aus der Datenbanksequenz sind lesbar und aufsteigend")
    void erzeugtAufsteigendeNummern() {
        String erste = generator.erzeuge(BESTELLDATUM);
        String zweite = generator.erzeuge(BESTELLDATUM);

        assertThat(erste).matches("B-2026-\\d{6}");
        assertThat(zweite).matches("B-2026-\\d{6}");
        assertThat(nummer(zweite)).isGreaterThan(nummer(erste));
    }

    @Test
    @DisplayName("D-13: Bestellnummern bleiben bei parallelen Aufrufen eindeutig")
    void bleibtNebenlaeufigEindeutig() throws Exception {
        List<Callable<String>> aufrufe =
                IntStream.range(0, 200)
                        .<Callable<String>>mapToObj(i -> () -> generator.erzeuge(BESTELLDATUM))
                        .toList();

        List<String> nummern;
        try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
            nummern =
                    pool.invokeAll(aufrufe).stream()
                            .map(SequenzBestellnummernZaehlerIT::ergebnis)
                            .toList();
        }

        assertThat(nummern).hasSize(200).doesNotHaveDuplicates();
    }

    private static long nummer(String bestellnummer) {
        return Long.parseLong(bestellnummer.substring("B-2026-".length()));
    }

    private static String ergebnis(Future<String> zukunft) {
        try {
            return zukunft.get();
        } catch (Exception fehler) {
            throw new IllegalStateException(fehler);
        }
    }
}
