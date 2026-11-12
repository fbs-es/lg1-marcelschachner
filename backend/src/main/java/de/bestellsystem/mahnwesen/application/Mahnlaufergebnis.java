package de.bestellsystem.mahnwesen.application;

import java.time.LocalDate;

/** Zusammenfassung eines Mahnlaufs über alle Kunden (US-06.2). */
public record Mahnlaufergebnis(LocalDate stichtag, int gemahnteKunden, int fehlgeschlageneKunden) {}
