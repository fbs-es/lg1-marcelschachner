package de.bestellsystem.gemeinsam.persistence;

import de.bestellsystem.gemeinsam.application.BestellnummernZaehler;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Zählt über eine PostgreSQL-Sequenz; nebenläufig sicher, auch über mehrere Instanzen. */
@Repository
class SequenzBestellnummernZaehler implements BestellnummernZaehler {

    private final JdbcClient jdbcClient;

    SequenzBestellnummernZaehler(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public long naechsterWert() {
        return jdbcClient.sql("SELECT nextval('bestellnummer_seq')").query(Long.class).single();
    }
}
