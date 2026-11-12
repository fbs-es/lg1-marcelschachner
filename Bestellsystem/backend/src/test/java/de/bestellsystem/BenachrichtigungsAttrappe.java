package de.bestellsystem;

import de.bestellsystem.gemeinsam.application.Benachrichtigung;
import de.bestellsystem.gemeinsam.application.BenachrichtigungNichtVersendetException;
import de.bestellsystem.gemeinsam.application.Benachrichtigungsversand;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Merkt sich versendete Nachrichten und lässt auf Wunsch einzelne Empfänger scheitern. */
public class BenachrichtigungsAttrappe implements Benachrichtigungsversand {

    private final List<Benachrichtigung> versendet = new CopyOnWriteArrayList<>();
    private final Set<String> scheiternde = ConcurrentHashMap.newKeySet();

    @Override
    public void sende(Benachrichtigung benachrichtigung) {
        if (scheiternde.contains(benachrichtigung.empfaenger())) {
            throw new BenachrichtigungNichtVersendetException(
                    "Attrappe: Versand an " + benachrichtigung.empfaenger() + " scheitert",
                    new IllegalStateException("SMTP nicht erreichbar"));
        }
        versendet.add(benachrichtigung);
    }

    public List<Benachrichtigung> versendet() {
        return List.copyOf(versendet);
    }

    public void scheitereFuer(String empfaenger) {
        scheiternde.add(empfaenger);
    }

    public void zuruecksetzen() {
        versendet.clear();
        scheiternde.clear();
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class Konfiguration {

        @Bean
        @Primary
        BenachrichtigungsAttrappe benachrichtigungsAttrappe() {
            return new BenachrichtigungsAttrappe();
        }
    }
}
