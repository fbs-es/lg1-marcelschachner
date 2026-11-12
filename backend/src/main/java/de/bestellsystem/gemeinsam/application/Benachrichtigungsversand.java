package de.bestellsystem.gemeinsam.application;

/** Port für den Versand von Benachrichtigungen (D-03); im Test durch eine Attrappe ersetzbar. */
public interface Benachrichtigungsversand {

    void sende(Benachrichtigung benachrichtigung);
}
