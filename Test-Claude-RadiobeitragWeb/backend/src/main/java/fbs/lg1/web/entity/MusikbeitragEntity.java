package fbs.lg1.web.entity;

import fbs.lg1.Musikgenre;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Datenbank-Abbild eines {@link fbs.lg1.Musikbeitrag}s.
 */
@Entity
@DiscriminatorValue("MUSIK")
public class MusikbeitragEntity extends RadiobeitragEntity {

    private String kuenstler;
    private String album;

    @Enumerated(EnumType.STRING)
    private Musikgenre genre;

    /**
     * Wird nur von JPA verwendet.
     */
    protected MusikbeitragEntity() {
    }

    /**
     * Legt ein Abbild mit allen Daten an.
     */
    public MusikbeitragEntity(String titel, int dauer, String kuenstler, String album, Musikgenre genre) {
        super(titel, dauer);
        this.kuenstler = kuenstler;
        this.album = album;
        this.genre = genre;
    }

    /**
     * @return der Name des Künstlers
     */
    public String showKuenstler() {
        return kuenstler;
    }

    /**
     * @return das Album
     */
    public String showAlbum() {
        return album;
    }

    /**
     * @return das Musikgenre
     */
    public Musikgenre showGenre() {
        return genre;
    }
}
