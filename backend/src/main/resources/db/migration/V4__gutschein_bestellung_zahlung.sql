-- E04 + E08: Gutschein, Bestellung, Positionen, Zahlungen (D-01 bis D-05, D-08 bis D-11, D-17)
CREATE TABLE gutschein (
    id              UUID         PRIMARY KEY,
    -- gespeichert getrimmt und großgeschrieben (D-17), daher genügt ein einfacher Unique-Constraint
    code            VARCHAR(50)  NOT NULL CONSTRAINT gutschein_code_eindeutig UNIQUE,
    rabatt_art      VARCHAR(20)  NOT NULL CHECK (rabatt_art IN ('PROZENT', 'FESTBETRAG')),
    prozent         NUMERIC(5,2),
    festbetrag_cent BIGINT,
    ablaufdatum     DATE         NOT NULL,
    CONSTRAINT gutschein_rabattwert CHECK (
        (rabatt_art = 'PROZENT' AND prozent > 0 AND prozent <= 100 AND festbetrag_cent IS NULL)
        OR (rabatt_art = 'FESTBETRAG' AND festbetrag_cent > 0 AND prozent IS NULL))
);

CREATE TABLE bestellung (
    bestellnummer       VARCHAR(30)  PRIMARY KEY,
    version             BIGINT       NOT NULL,
    kunde_id            UUID         NOT NULL REFERENCES kunde (id),
    bestelldatum        DATE         NOT NULL,
    faellig_am          DATE         NOT NULL,
    -- Snapshot der Lieferadresse, keine Referenz auf adresse (D-08)
    liefer_strasse      VARCHAR(200) NOT NULL,
    liefer_stadt        VARCHAR(100) NOT NULL,
    liefer_postleitzahl VARCHAR(20)  NOT NULL,
    liefer_land         VARCHAR(2)   NOT NULL,
    gutschein_id        UUID         REFERENCES gutschein (id),
    rabatt_betrag_cent  BIGINT       NOT NULL CHECK (rabatt_betrag_cent >= 0)
);

CREATE INDEX bestellung_kunde_idx ON bestellung (kunde_id);
CREATE INDEX bestellung_gutschein_idx ON bestellung (gutschein_id);

CREATE TABLE bestellposition (
    id               UUID        PRIMARY KEY,
    bestellnummer    VARCHAR(30) NOT NULL REFERENCES bestellung (bestellnummer) ON DELETE CASCADE,
    zeilennummer     INTEGER     NOT NULL CHECK (zeilennummer >= 1),
    produkt_id       UUID        NOT NULL REFERENCES produkt (id),
    menge            INTEGER     NOT NULL CHECK (menge >= 1),
    einzelpreis_cent BIGINT      NOT NULL CHECK (einzelpreis_cent >= 0),
    CONSTRAINT bestellposition_zeile_eindeutig UNIQUE (bestellnummer, zeilennummer),
    CONSTRAINT bestellposition_produkt_eindeutig UNIQUE (bestellnummer, produkt_id)
);

CREATE INDEX bestellposition_produkt_idx ON bestellposition (produkt_id);

CREATE TABLE zahlung (
    id                 UUID        PRIMARY KEY,
    bestellnummer      VARCHAR(30) NOT NULL REFERENCES bestellung (bestellnummer) ON DELETE CASCADE,
    zahlungsart        VARCHAR(20) NOT NULL
        CHECK (zahlungsart IN ('KREDITKARTE', 'PAYPAL', 'LASTSCHRIFT', 'RECHNUNG', 'VORKASSE')),
    buchungsart        VARCHAR(20) NOT NULL CHECK (buchungsart IN ('ZAHLUNG', 'ERSTATTUNG')),
    transaktionsstatus VARCHAR(20) NOT NULL
        CHECK (transaktionsstatus IN ('AUSSTEHEND', 'ERFOLGREICH', 'FEHLGESCHLAGEN', 'STORNIERT')),
    betrag_cent        BIGINT      NOT NULL CHECK (betrag_cent >= 0),
    zahlungszeitpunkt  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX zahlung_bestellnummer_idx ON zahlung (bestellnummer);
