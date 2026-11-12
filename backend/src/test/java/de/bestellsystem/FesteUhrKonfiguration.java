package de.bestellsystem;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Legt Datum und Uhrzeit für Integrationstests fest. */
@TestConfiguration(proxyBeanMethods = false)
public class FesteUhrKonfiguration {

    public static final Instant JETZT = Instant.parse("2026-03-15T09:30:00Z");
    public static final ZoneId ZEITZONE = ZoneId.of("Europe/Berlin");

    @Bean
    @Primary
    Clock festeUhr() {
        return Clock.fixed(JETZT, ZEITZONE);
    }
}
