package de.bestellsystem.gemeinsam.application;

/** Port für den Versand von Benachrichtigungen (D-03); im Test durch eine Attrappe ersetzbar. */
public interface Benachrichtigungsversand {

    /**
     * @throws BenachrichtigungNichtVersendetException wenn die Nachricht nicht zugestellt werden
     *     konnte
     */
    void sende(Benachrichtigung benachrichtigung);
}
