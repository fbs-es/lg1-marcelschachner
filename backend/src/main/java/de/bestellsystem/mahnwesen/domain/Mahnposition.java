package de.bestellsystem.mahnwesen.domain;

import de.bestellsystem.gemeinsam.domain.Geld;
import java.time.LocalDate;

/** Eine gemahnte Bestellung mit ihrem offenen Betrag am Stichtag. */
public record Mahnposition(String bestellnummer, LocalDate faelligAm, Geld offenerBetrag) {}
