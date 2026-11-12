package de.bestellsystem.gemeinsam.application;

/** Der Versand einer Benachrichtigung ist gescheitert; Vertrag des Ports (D-20). */
public class BenachrichtigungNichtVersendetException extends RuntimeException {

    public BenachrichtigungNichtVersendetException(String nachricht, Throwable ursache) {
        super(nachricht, ursache);
    }
}
