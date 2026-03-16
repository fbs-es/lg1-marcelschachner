package fbs.lg1.suchensortieren;

import java.util.Date;

public class Schueler implements Comparable<Schueler> {
    @Override
    public int compareTo(Schueler other) {
        return this.alter.compareTo(other.alter);
    }

    String name;
    String vorname;
    Integer alter;
    java.util.Date geburtsdatum;

    public Schueler(String name, String vorname, Integer alter, java.util.Date geburtsdatum) {
        this.name = name;
        this.vorname = vorname;
        this.alter = alter;
        this.geburtsdatum = geburtsdatum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public Integer getAlter() {
        return alter;
    }

    public void setAlter(Integer alter) {
        this.alter = alter;
    }

    public Date getGeburtsdatum() {
        return geburtsdatum;
    }

    public void setGeburtsdatum(Date geburtsdatum) {
        this.geburtsdatum = geburtsdatum;
    }

}
