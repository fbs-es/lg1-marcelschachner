package fbs.lg1.hashing;

import java.util.HashMap;

public class Hashing2 {
  public void run() {
    HashMap<Kunde, String> kundenListe = new HashMap<>();
    Kunde max = new Kunde("Mustermann", "Max", 123456789, "Herr Müller");
    Kunde marcel = new Kunde("Schachner", "Marcel Ferdinand", 987654321, "Frau Schmidt");
    Kunde melissa = new Kunde("Avdic", "Melissa", 111222333, "Herr Fischer");

    kundenListe.put(max, max.betreuer);
    kundenListe.put(marcel, marcel.betreuer);
    kundenListe.put(melissa, melissa.betreuer);

    kundenListe.forEach((k, v) -> System.out.println(k.name + " " + k.vorname + ": " + v));
  }
}
