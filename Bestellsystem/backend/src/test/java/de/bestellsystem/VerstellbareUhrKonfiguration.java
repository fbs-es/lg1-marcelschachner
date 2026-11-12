package de.bestellsystem;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Uhr, die ein Test auf verschiedene Tage stellen kann (Merkdatum US-03.1, Lieferstatus US-07.2).
 */
@TestConfiguration(proxyBeanMethods = false)
public class VerstellbareUhrKonfiguration {

    @Bean
    @Primary
    VerstellbareUhr verstellbareUhr() {
        return new VerstellbareUhr(ZoneId.of("Europe/Berlin"));
    }

    public static final class VerstellbareUhr extends Clock {

        private final ZoneId zone;
        private volatile Instant jetzt = Instant.parse("2026-11-12T09:30:00Z");

        VerstellbareUhr(ZoneId zone) {
            this.zone = zone;
        }

        public void stelle(Instant zeitpunkt) {
            jetzt = zeitpunkt;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId andereZone) {
            VerstellbareUhr uhr = new VerstellbareUhr(andereZone);
            uhr.stelle(jetzt);
            return uhr;
        }

        @Override
        public Instant instant() {
            return jetzt;
        }
    }
}
