package de.bestellsystem;

import org.springframework.jdbc.core.simple.JdbcClient;

/** Leert die Fachtabellen in Abhängigkeitsreihenfolge, damit sich Testklassen nicht stören. */
public final class Datenbestand {

    private Datenbestand() {}

    public static void leere(JdbcClient jdbc) {
        jdbc.sql("DELETE FROM bestellung").update();
        jdbc.sql("DELETE FROM gutschein").update();
        jdbc.sql("DELETE FROM kunde").update();
        jdbc.sql("DELETE FROM produkt").update();
    }
}
