package de.bestellsystem.versand.domain;

import java.util.List;

/**
 * Lieferstatus eines Versandpakets (D-12, D-22): nur vorwärts, Stufen dürfen übersprungen werden;
 * RETOURE und VERLOREN erst nach der Übergabe; ZUGESTELLT, RETOURE, VERLOREN sind Endzustände.
 */
public enum Lieferstatus {
    ANGELEGT,
    UEBERGEBEN,
    IN_ZUSTELLUNG,
    ZUGESTELLT,
    RETOURE,
    VERLOREN;

    public List<Lieferstatus> erlaubteFolgestatus() {
        return switch (this) {
            case ANGELEGT -> List.of(UEBERGEBEN, IN_ZUSTELLUNG, ZUGESTELLT);
            case UEBERGEBEN -> List.of(IN_ZUSTELLUNG, ZUGESTELLT, RETOURE, VERLOREN);
            case IN_ZUSTELLUNG -> List.of(ZUGESTELLT, RETOURE, VERLOREN);
            case ZUGESTELLT, RETOURE, VERLOREN -> List.of();
        };
    }

    public boolean kannWechselnZu(Lieferstatus ziel) {
        return erlaubteFolgestatus().contains(ziel);
    }
}
