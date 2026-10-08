package fbs.lg1;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Eine städtische Bibliothek, die ihren eigenen Buchbestand, ihre registrierten {@link Nutzer} und
 * alle gestarteten {@link Ausleihe}n verwaltet.
 */
public class Bibliothek {

    private final String name;
    private final String adresse;
    private final List<Buch> buecher = new ArrayList<>();
    private final List<Nutzer> nutzer = new ArrayList<>();
    private final List<Ausleihe> ausleihen = new ArrayList<>();

    /**
     * Legt eine Bibliothek ohne Bücher, Nutzer und Ausleihen an.
     *
     * @throws IllegalArgumentException wenn Name oder Adresse leer sind
     */
    public Bibliothek(String name, String adresse) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }
        if (adresse == null || adresse.isBlank()) {
            throw new IllegalArgumentException("Adresse darf nicht leer sein!");
        }

        this.name = name;
        this.adresse = adresse;

        checkInvariant();
    }

    /**
     * Legt ein neues Buch an und nimmt es in den Bestand auf.
     *
     * @return das neue, verfügbare Buch
     * @throws IllegalArgumentException wenn ISBN, Titel oder Autor leer sind
     * @throws IllegalStateException wenn bereits ein Buch mit dieser ISBN im Bestand ist
     */
    public Buch buchHinzufuegen(String isbn, String titel, String autor) {
        if (findBuch(isbn) != null) {
            throw new IllegalStateException("Buch mit dieser ISBN ist bereits im Bestand!");
        }

        int oldAnzahl = buecher.size();

        Buch buch = new Buch(isbn, titel, autor);
        buecher.add(buch);

        assert buecher.size() == oldAnzahl + 1 : "Fehler: Buch wurde nicht hinzugefügt!";
        checkInvariant();
        return buch;
    }

    /**
     * Registriert einen Nutzer.
     *
     * @throws IllegalArgumentException wenn der Nutzer fehlt
     * @throws IllegalStateException wenn bereits ein Nutzer mit dieser ID registriert ist
     */
    public void nutzerRegistrieren(Nutzer nutzer) {
        if (nutzer == null) {
            throw new IllegalArgumentException("Nutzer darf nicht fehlen!");
        }
        if (findNutzer(nutzer.showId()) != null) {
            throw new IllegalStateException("Nutzer mit dieser ID ist bereits registriert!");
        }

        int oldAnzahl = this.nutzer.size();

        this.nutzer.add(nutzer);

        assert this.nutzer.size() == oldAnzahl + 1 : "Fehler: Nutzer wurde nicht registriert!";
        checkInvariant();
    }

    /**
     * Startet eine Ausleihe eines Buchs aus dem Bestand und speichert sie.
     *
     * @return die neue aktive Ausleihe
     * @throws IllegalArgumentException wenn Nutzer, Buch oder Ausleihdatum fehlen
     * @throws IllegalStateException wenn der Nutzer nicht registriert ist, das Buch nicht im
     *     Bestand ist oder das Buch nicht verfügbar ist
     */
    public Ausleihe ausleiheStarten(Nutzer nutzer, Buch buch, Date ausleihdatum) {
        if (nutzer == null) {
            throw new IllegalArgumentException("Nutzer darf nicht fehlen!");
        }
        if (buch == null) {
            throw new IllegalArgumentException("Buch darf nicht fehlen!");
        }
        if (ausleihdatum == null) {
            throw new IllegalArgumentException("Ausleihdatum darf nicht fehlen!");
        }
        if (findNutzer(nutzer.showId()) != nutzer) {
            throw new IllegalStateException("Nutzer ist nicht registriert!");
        }
        if (findBuch(buch.showIsbn()) != buch) {
            throw new IllegalStateException("Buch ist nicht im Bestand!");
        }
        if (!buch.istVerfuegbar()) {
            throw new IllegalStateException("Buch ist nicht verfügbar!");
        }

        int oldAnzahl = ausleihen.size();

        Ausleihe ausleihe = new Ausleihe(ausleihdatum, nutzer, buch);
        ausleihen.add(ausleihe);

        assert ausleihen.size() == oldAnzahl + 1 : "Fehler: Ausleihe wurde nicht gespeichert!";
        assert !buch.istVerfuegbar() : "Fehler: Buch wurde nicht ausgeliehen!";
        checkInvariant();
        return ausleihe;
    }

    private Buch findBuch(String isbn) {
        for (Buch buch : buecher) {
            if (buch.showIsbn().equals(isbn)) {
                return buch;
            }
        }
        return null;
    }

    private Nutzer findNutzer(int id) {
        for (Nutzer eintrag : nutzer) {
            if (eintrag.showId() == id) {
                return eintrag;
            }
        }
        return null;
    }

    private void checkInvariant() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name darf nicht leer sein!");
        }
        if (adresse == null || adresse.isBlank()) {
            throw new IllegalStateException("Adresse darf nicht leer sein!");
        }
        for (Buch buch : buecher) {
            if (buch == null) {
                throw new IllegalStateException("Bestand enthält ein fehlendes Buch!");
            }
        }
        for (Nutzer eintrag : nutzer) {
            if (eintrag == null) {
                throw new IllegalStateException("Nutzerliste enthält einen fehlenden Nutzer!");
            }
        }
        for (Ausleihe ausleihe : ausleihen) {
            if (ausleihe == null) {
                throw new IllegalStateException("Ausleihliste enthält eine fehlende Ausleihe!");
            }
        }
    }

    /** @return der Name der Bibliothek */
    public String showName() {
        return name;
    }

    /** @return die Adresse der Bibliothek */
    public String showAdresse() {
        return adresse;
    }

    /** @return alle Bücher im Bestand, nicht verändern */
    public List<Buch> showBuecher() {
        return buecher;
    }

    /** @return alle registrierten Nutzer, nicht verändern */
    public List<Nutzer> showNutzer() {
        return nutzer;
    }

    /** @return alle gestarteten Ausleihen, nicht verändern */
    public List<Ausleihe> showAusleihen() {
        return ausleihen;
    }
}
