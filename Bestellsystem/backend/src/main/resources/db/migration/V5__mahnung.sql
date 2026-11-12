-- E06: Mahnung je Bestellung mit eingefrorenem Datum und Betrag (D-03, D-20)
CREATE TABLE mahnung (
    id                    UUID        PRIMARY KEY,
    bestellnummer         VARCHAR(30) NOT NULL REFERENCES bestellung (bestellnummer) ON DELETE CASCADE,
    erstellt_am           DATE        NOT NULL,
    gemahnter_betrag_cent BIGINT      NOT NULL CHECK (gemahnter_betrag_cent > 0)
);

CREATE INDEX mahnung_bestellnummer_idx ON mahnung (bestellnummer);

-- Mahnlauf sucht Kunden mit überfälligen Bestellungen (D-20)
CREATE INDEX bestellung_faellig_am_idx ON bestellung (faellig_am);
