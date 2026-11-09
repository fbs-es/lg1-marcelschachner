package fbs.lg1;

/**
 * Ein {@link Radiobeitrag}, der ein Musikstück mit Künstler, Album und Genre spielt.
 */
public class Musikbeitrag extends Radiobeitrag {

    private final String kuenstler;
    private final String album;
    private final Musikgenre genre;

    /**
     * Legt einen Musikbeitrag an.
     *
     * @throws IllegalArgumentException wenn Titel, Künstler oder Album leer sind, die Dauer nicht
     *     größer als 0 ist oder das Genre fehlt
     */
    public Musikbeitrag(String titel, int dauer, String kuenstler, String album, Musikgenre genre) {
        super(titel, dauer);
        if (kuenstler == null || kuenstler.isBlank()) {
            throw new IllegalArgumentException("Künstler darf nicht leer sein!");
        }
        if (album == null || album.isBlank()) {
            throw new IllegalArgumentException("Album darf nicht leer sein!");
        }
        if (genre == null) {
            throw new IllegalArgumentException("Genre darf nicht fehlen!");
        }

        this.kuenstler = kuenstler;
        this.album = album;
        this.genre = genre;

        checkInvariant();
    }

    /**
     * Erstellt einen Musikbeitrag mit denselben Daten, dem Zusatz "(Fortsetzung)" im Titel und der
     * angegebenen Restdauer.
     */
    @Override
    protected Radiobeitrag erstelleFortsetzung(int restdauer) {
        String neuerTitel = showTitel() + " (Fortsetzung)";
        return new Musikbeitrag(neuerTitel, restdauer, kuenstler, album, genre);
    }

    private void checkInvariant() {
        if (kuenstler == null || kuenstler.isBlank()) {
            throw new IllegalStateException("Künstler darf nicht leer sein!");
        }
        if (album == null || album.isBlank()) {
            throw new IllegalStateException("Album darf nicht leer sein!");
        }
        if (genre == null) {
            throw new IllegalStateException("Genre darf nicht fehlen!");
        }
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
