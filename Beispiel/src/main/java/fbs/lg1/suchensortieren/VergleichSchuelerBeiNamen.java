package fbs.lg1.suchensortieren;

import java.util.Comparator;

public class VergleichSchuelerBeiNamen implements Comparator<Schueler> {
  @Override
  public int compare(Schueler s1, Schueler s2) {
    return s1.getName().compareTo(s2.getName());
  }
}
