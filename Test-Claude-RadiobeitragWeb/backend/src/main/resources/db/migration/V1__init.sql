CREATE TABLE radioprogramm (
    id BIGINT NOT NULL AUTO_INCREMENT,
    datum DATE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE radiosender (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    frequenz DOUBLE NOT NULL,
    programm_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_radiosender_programm UNIQUE (programm_id),
    CONSTRAINT fk_radiosender_programm FOREIGN KEY (programm_id) REFERENCES radioprogramm (id)
);

CREATE TABLE radiosendung (
    id BIGINT NOT NULL AUTO_INCREMENT,
    programm_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    moderator VARCHAR(255) NOT NULL,
    startzeit TIME NOT NULL,
    endzeit TIME NOT NULL,
    maximaldauer INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_radiosendung_programm FOREIGN KEY (programm_id) REFERENCES radioprogramm (id)
);

CREATE TABLE radiobeitrag (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sendung_id BIGINT,
    position_nr INT,
    typ VARCHAR(31) NOT NULL,
    titel VARCHAR(255) NOT NULL,
    dauer INT NOT NULL,
    kuenstler VARCHAR(255),
    album VARCHAR(255),
    genre VARCHAR(31),
    kunde VARCHAR(255),
    werbebudget DOUBLE,
    sprechertext VARCHAR(2000),
    dringlichkeitsstufe INT,
    region VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_radiobeitrag_sendung FOREIGN KEY (sendung_id) REFERENCES radiosendung (id)
);
