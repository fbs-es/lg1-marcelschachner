package de.bestellsystem.gemeinsam.infrastruktur;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Aktiviert zeitgesteuerte Läufe wie den Mahnlauf (D-20). */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
class ZeitplanKonfiguration {}
