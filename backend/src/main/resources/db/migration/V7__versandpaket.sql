-- E07: Versandpakete je Bestellung (D-12, D-22)
CREATE TABLE versandpaket (
    id                  UUID        PRIMARY KEY,
    bestellnummer       VARCHAR(30) NOT NULL REFERENCES bestellung (bestellnummer) ON DELETE CASCADE,
    -- von Hibernate nach dem Einfügen gesetzt (@OrderColumn), daher nullable
    reihenfolge         INTEGER,
    dienstleister       VARCHAR(20) NOT NULL
        CHECK (dienstleister IN ('DHL', 'DPD', 'GLS', 'HERMES', 'UPS')),
    sendungsnummer      VARCHAR(50) NOT NULL CHECK (sendungsnummer ~ '^[A-Z0-9]{1,50}$'),
    lieferstatus        VARCHAR(20) NOT NULL
        CHECK (lieferstatus IN ('ANGELEGT', 'UEBERGEBEN', 'IN_ZUSTELLUNG', 'ZUGESTELLT', 'RETOURE', 'VERLOREN')),
    angelegt_am         TIMESTAMPTZ NOT NULL,
    status_geaendert_am TIMESTAMPTZ NOT NULL,
    -- I-11: Sendungsnummer eindeutig je Dienstleister, über alle Bestellungen
    CONSTRAINT versandpaket_sendungsnummer_eindeutig UNIQUE (dienstleister, sendungsnummer)
);

CREATE INDEX versandpaket_bestellnummer_idx ON versandpaket (bestellnummer);
