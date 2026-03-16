package fbs.lg1.suchensortieren;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class SortArrays {
  public void run() {
    int[] zahlen = { 5, 2, 8, 1, 7 };
    System.out.println("Unsortiertes Array: " + Arrays.toString(zahlen));
    Arrays.sort(zahlen);
    System.out.println("Sortiertes Array: " + Arrays.toString(zahlen));
    // nicht sortiert - binarySearch kein Ergebnis: -1
    int index = Arrays.binarySearch(zahlen, 2);
    System.out.println("Index von 2: " + index);

    String[] woerter = { "Banane", "Apfel", "Orange", "Birne" };
    // Collections - kann alles sortieren. Maps, LinkedList, ArrayList.
    System.out.println("Unsortiertes Array: " + Arrays.toString(woerter));
    Collections.sort(Arrays.asList(woerter));
    System.out.println("Sortiertes Array: " + Arrays.toString(woerter));
    System.out.println("Index von Orange: " + Arrays.binarySearch(woerter, "Orange"));
    // kann out of the box verwenden wegen vordefinierter Ordnungsrelation
    ArrayList<String> schueler = new ArrayList<>();
    schueler.add("Felix");
    schueler.add("Alexander");
    schueler.add("Marco");
    Collections.sort(schueler);
    System.out.println("Sortierte Liste: " + schueler);

    // Comparable und Comparator
  }
}
