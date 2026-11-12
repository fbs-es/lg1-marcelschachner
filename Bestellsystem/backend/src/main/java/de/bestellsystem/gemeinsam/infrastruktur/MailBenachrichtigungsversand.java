package de.bestellsystem.gemeinsam.infrastruktur;

import de.bestellsystem.gemeinsam.application.Benachrichtigung;
import de.bestellsystem.gemeinsam.application.BenachrichtigungNichtVersendetException;
import de.bestellsystem.gemeinsam.application.Benachrichtigungsversand;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Versendet Benachrichtigungen als Text-E-Mail über SMTP (lokal: Mailpit). */
@Component
class MailBenachrichtigungsversand implements Benachrichtigungsversand {

    private final JavaMailSender mailSender;
    private final MailEinstellungen einstellungen;

    MailBenachrichtigungsversand(JavaMailSender mailSender, MailEinstellungen einstellungen) {
        this.mailSender = mailSender;
        this.einstellungen = einstellungen;
    }

    @Override
    public void sende(Benachrichtigung benachrichtigung) {
        SimpleMailMessage nachricht = new SimpleMailMessage();
        nachricht.setFrom(einstellungen.absender());
        nachricht.setTo(benachrichtigung.empfaenger());
        nachricht.setSubject(benachrichtigung.betreff());
        nachricht.setText(benachrichtigung.text());
        try {
            mailSender.send(nachricht);
        } catch (MailException fehler) {
            throw new BenachrichtigungNichtVersendetException(
                    "E-Mail an " + benachrichtigung.empfaenger() + " nicht versendet", fehler);
        }
    }
}
