package de.bestellsystem.gemeinsam.infrastruktur;

import de.bestellsystem.gemeinsam.application.FachlicheEinstellungen;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class UhrKonfiguration {

    @Bean
    Clock uhr(FachlicheEinstellungen einstellungen) {
        return Clock.system(einstellungen.zeitzone());
    }
}
