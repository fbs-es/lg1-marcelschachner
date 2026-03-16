package fbs.lg1.suchensortieren;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Bibliothek {
  public void run() {
    Buch[] buecher = {
        new Buch("978-3-16-148410-0", "Buch C", "Band 2"),
        new Buch("978-1-23-456789-7", "Buch B", "Band 2"),
        new Buch("978-0-12-345678-9", "Buch A", "Band 2"),
        new Buch("978-0-12-345678-9", "Buch A", "Band 1")
    };
    Collections.sort(Arrays.asList(buecher), Comparator.comparing((Buch b) -> b.title).thenComparing(b -> b.band));
    Arrays.asList(buecher).forEach(b -> System.out.println(b.title + " (" + b.isbn + ") - " + b.band));
  };
}