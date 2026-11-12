package de.bestellsystem.gemeinsam.infrastruktur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import de.bestellsystem.gemeinsam.application.Benachrichtigung;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
}
