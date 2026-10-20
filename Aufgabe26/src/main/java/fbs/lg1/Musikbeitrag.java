package fbs.lg1;

/**
 * Ein Musiktitel als {@link Radiobeitrag} mit Künstler, Album und Genre.
 */
public class Musikbeitrag extends Radiobeitrag {

    private final String kuenstler;
    private final String album;
    private final String genre;

    /**
     * Legt einen Musikbeitrag an.
     *
     * @throws IllegalArgumentException wenn Titel, Künstler, Album oder Genre leer sind oder die
     *     Dauer nicht größer als 0 ist
     */
    public Musikbeitrag(String titel, int dauer, String kuenstler, String album, String genre) {
        super(titel, dauer);
        if (kuenstler == null || kuenstler.isBlank()) {
            throw new IllegalArgumentException("Künstler darf nicht leer sein!");
        }
        if (album == null || album.isBlank()) {
            throw new IllegalArgumentException("Album darf nicht leer sein!");
        }
        if (genre == null || genre.isBlank()) {
            throw new IllegalArgumentException("Genre darf nicht leer sein!");
        }

        this.kuenstler = kuenstler;
        this.album = album;
        this.genre = genre;

        checkInvariant();
    }

    /**
     * Erzeugt einen Musikbeitrag mit gleichem Titel, Künstler, Album und Genre.
     *
     * @param dauer die Dauer der Kopie in Sekunden
     * @return der neue Musikbeitrag
     */
    @Override
    public Radiobeitrag kopieMitDauer(int dauer) {
        String titel = showTitel();
        Musikbeitrag kopie = new Musikbeitrag(titel, dauer, kuenstler, album, genre);
        return kopie;
    }

    private void checkInvariant() {
        if (kuenstler == null || kuenstler.isBlank()) {
            throw new IllegalStateException("Künstler darf nicht leer sein!");
        }
        if (album == null || album.isBlank()) {
            throw new IllegalStateException("Album darf nicht leer sein!");
        }
        if (genre == null || genre.isBlank()) {
            throw new IllegalStateException("Genre darf nicht leer sein!");
        }
    }
}
