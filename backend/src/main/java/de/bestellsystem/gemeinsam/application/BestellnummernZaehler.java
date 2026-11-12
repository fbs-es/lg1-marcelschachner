package de.bestellsystem.gemeinsam.application;

/** Liefert bei jedem Aufruf einen neuen, eindeutigen, aufsteigenden Wert (D-14). */
@FunctionalInterface
public interface BestellnummernZaehler {

    long naechsterWert();
}
