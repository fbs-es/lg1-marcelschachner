package de.bestellsystem.gemeinsam.application;

import java.time.Period;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Konfigurierbare Fachparameter (D-03).
 *
 * @param zeitzone Zeitzone der Uhr, bestimmt Stichtage
 * @param zahlungsziel Abstand Bestelldatum bis Fälligkeit
 * @param mahnintervall Mindestabstand zwischen zwei Mahnungen derselben Bestellung
 */
@ConfigurationProperties("bestellsystem")
public record FachlicheEinstellungen(
        @DefaultValue("Europe/Berlin") ZoneId zeitzone,
        @DefaultValue("14d") Period zahlungsziel,
        @DefaultValue("7d") Period mahnintervall) {}
