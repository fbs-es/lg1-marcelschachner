package de.bestellsystem.gemeinsam.infrastruktur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import de.bestellsystem.gemeinsam.application.Benachrichtigung;
import de.bestellsystem.gemeinsam.application.BenachrichtigungNichtVersendetException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class MailBenachrichtigungsversandTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final MailBenachrichtigungsversand versand =
            new MailBenachrichtigungsversand(
                    mailSender, new MailEinstellungen("shop@bestellsystem.local"));

    @Test
    void versendetBenachrichtigungAlsTextMail() {
        versand.sende(new Benachrichtigung("kunde@example.org", "Zahlungserinnerung", "Hallo"));

        ArgumentCaptor<SimpleMailMessage> nachricht =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(nachricht.capture());
        assertThat(nachricht.getValue().getFrom()).isEqualTo("shop@bestellsystem.local");
        assertThat(nachricht.getValue().getTo()).containsExactly("kunde@example.org");
        assertThat(nachricht.getValue().getSubject()).isEqualTo("Zahlungserinnerung");
        assertThat(nachricht.getValue().getText()).isEqualTo("Hallo");
    }

    @Test
    void uebersetztMailfehlerInFehlerDesPorts() {
        doThrow(new MailSendException("SMTP nicht erreichbar"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        assertThatThrownBy(() ->
                        versand.sende(new Benachrichtigung("kunde@example.org", "Betreff", "Text")))
                .isInstanceOf(BenachrichtigungNichtVersendetException.class)
                .hasMessageContaining("kunde@example.org")
                .hasCauseInstanceOf(MailSendException.class);
    }
}
