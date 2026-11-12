-- E01: Kundenkonto und Adressbuch (D-06, D-13, D-16, I-1 bis I-3)
CREATE TABLE kunde (
    id       UUID         PRIMARY KEY,
    version  BIGINT       NOT NULL,
    vorname  VARCHAR(100) NOT NULL,
    nachname VARCHAR(100) NOT NULL,
    -- gespeichert getrimmt und kleingeschrieben, daher genügt ein einfacher Unique-Constraint
    email    VARCHAR(254) NOT NULL CONSTRAINT kunde_email_eindeutig UNIQUE
);

CREATE INDEX kunde_nachname_idx ON kunde (nachname, vorname);

CREATE TABLE adresse (
    id                   UUID         PRIMARY KEY,
    kunde_id             UUID         NOT NULL REFERENCES kunde (id) ON DELETE CASCADE,
    -- von Hibernate nach dem Einfügen gesetzt (@OrderColumn), daher nullable
    reihenfolge          INTEGER,
    strasse              VARCHAR(200) NOT NULL,
    stadt                VARCHAR(100) NOT NULL,
    postleitzahl         VARCHAR(20)  NOT NULL,
    land                 VARCHAR(2)   NOT NULL,
    ist_lieferadresse    BOOLEAN      NOT NULL,
    ist_rechnungsadresse BOOLEAN      NOT NULL,
    CONSTRAINT adresse_mindestens_ein_kennzeichen CHECK (ist_lieferadresse OR ist_rechnungsadresse)
);

CREATE INDEX adresse_kunde_idx ON adresse (kunde_id);
