-- E03: Merkliste als Assoziationsklasse Kunde *–* Produkt (D-07, D-21, I-4)
CREATE TABLE merklisteneintrag (
    kunde_id        UUID        NOT NULL REFERENCES kunde (id) ON DELETE CASCADE,
    -- Produkt aus dem Katalog entfernt: Eintrag verschwindet (D-07)
    produkt_id      UUID        NOT NULL REFERENCES produkt (id) ON DELETE CASCADE,
    -- Zeitpunkt statt Datum, damit "neueste zuerst" auch innerhalb eines Tages gilt (D-21)
    hinzugefuegt_am TIMESTAMPTZ NOT NULL,
    CONSTRAINT merklisteneintrag_pk PRIMARY KEY (kunde_id, produkt_id)
);

CREATE INDEX merklisteneintrag_produkt_idx ON merklisteneintrag (produkt_id);
