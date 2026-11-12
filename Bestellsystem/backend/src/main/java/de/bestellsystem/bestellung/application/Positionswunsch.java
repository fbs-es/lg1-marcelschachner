package de.bestellsystem.bestellung.application;

import java.util.UUID;

public record Positionswunsch(UUID produktId, int menge) {}
