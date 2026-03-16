package fbs.lg1.suchensortieren;

public class Buch implements Comparable<Buch> {
  public String isbn;
  public String title;
  public String band;

  public Buch(String isbn, String title, String band) {
    this.isbn = isbn;
    this.title = title;
    this.band = band;
  }

  @Override
  public int compareTo(Buch other) {
    return this.isbn.compareTo(other.isbn);
  }
}
