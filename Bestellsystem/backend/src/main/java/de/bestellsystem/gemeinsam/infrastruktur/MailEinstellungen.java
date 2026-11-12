package de.bestellsystem.gemeinsam.infrastruktur;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("bestellsystem.mail")
public record MailEinstellungen(@DefaultValue("shop@bestellsystem.local") String absender) {}
