package fbs.lg1.hashing;

import java.util.HashMap;

public class Hashing1 {
  public void run() {
    HashMap<Artikel, Double> artikelListe = new HashMap<>();
    Artikel apfel = new Artikel("Apfel", 1.0d);
    Artikel butter = new Artikel("Butter", 2.30d);
    Artikel avocado = new Artikel("Avocado", 2.0d);

    artikelListe.put(apfel, 1.0d);
    artikelListe.put(butter, 2.30d);
    artikelListe.put(avocado, 2.0d);

    artikelListe.forEach((k, v) -> System.out.println(k.getName() + ": " + v));
  }
}
