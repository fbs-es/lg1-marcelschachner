-- E02: Produktkatalog (D-09, D-15, I-7)
CREATE TABLE produkt (
    id               UUID         PRIMARY KEY,
    version          BIGINT       NOT NULL,
    titel            VARCHAR(200) NOT NULL,
    listenpreis_cent BIGINT       NOT NULL CHECK (listenpreis_cent >= 0),
    lagerbestand     INTEGER      NOT NULL CHECK (lagerbestand >= 0)
);

CREATE INDEX produkt_titel_idx ON produkt (titel);
