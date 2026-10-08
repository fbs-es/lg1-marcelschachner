package fbs.lg1;

/** Ein Buch aus dem Medienbestand einer {@link Bibliothek}. Ein neues Buch ist verfügbar. */
public class Buch {

    private final String isbn;
    private final String titel;
    private final String autor;
    private boolean verfuegbar;

    /**
     * Legt ein verfügbares Buch an.
     *
     * @throws IllegalArgumentException wenn ISBN, Titel oder Autor leer sind
     */
    public Buch(String isbn, String titel, String autor) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN darf nicht leer sein!");
        }
        if (titel == null || titel.isBlank()) {
            throw new IllegalArgumentException("Titel darf nicht leer sein!");
        }
        if (autor == null || autor.isBlank()) {
            throw new IllegalArgumentException("Autor darf nicht leer sein!");
        }

        this.isbn = isbn;
        this.titel = titel;
        this.autor = autor;
        this.verfuegbar = true;

        checkInvariant();
    }

    /**
     * Markiert das Buch als ausgeliehen. Wird von {@link Ausleihe} aufgerufen.
     *
     * @throws IllegalStateException wenn das Buch nicht verfügbar ist
     */
    public void ausleihen() {
        if (!verfuegbar) {
            throw new IllegalStateException("Buch ist bereits ausgeliehen!");
        }

        verfuegbar = false;

        assert !verfuegbar : "Fehler: Buch wurde nicht ausgeliehen!";
        checkInvariant();
    }

    /**
     * Markiert das Buch wieder als verfügbar. Wird von {@link Ausleihe} aufgerufen.
     *
     * @throws IllegalStateException wenn das Buch bereits verfügbar ist
     */
    public void zurueckgeben() {
        if (verfuegbar) {
            throw new IllegalStateException("Buch ist nicht ausgeliehen!");
        }

        verfuegbar = true;

        assert verfuegbar : "Fehler: Buch wurde nicht zurückgegeben!";
        checkInvariant();
    }

    private void checkInvariant() {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalStateException("ISBN darf nicht leer sein!");
        }
        if (titel == null || titel.isBlank()) {
            throw new IllegalStateException("Titel darf nicht leer sein!");
        }
        if (autor == null || autor.isBlank()) {
            throw new IllegalStateException("Autor darf nicht leer sein!");
        }
    }

    /** @return die eindeutige ISBN */
    public String showIsbn() {
        return isbn;
    }

    /** @return der Titel */
    public String showTitel() {
        return titel;
    }

    /** @return der Autor */
    public String showAutor() {
        return autor;
    }

    /** @return {@code true}, wenn das Buch ausgeliehen werden kann */
    public boolean istVerfuegbar() {
        return verfuegbar;
    }
}
