package fbs.lg1.hashing;

public class Artikel {
  private String name;
  private double empfohlenerVerkaufspreis;

  public Artikel(String name, double empfohlenerVerkaufspreis) {
    this.name = name;
    this.empfohlenerVerkaufspreis = empfohlenerVerkaufspreis;
  }

  public String getName() {
    return name;
  }

  public double getEmpfohlenerVerkaufspreis() {
    return empfohlenerVerkaufspreis;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
    // return ((Character) this.name.charAt(0)).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Artikel other = (Artikel) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (Double.doubleToLongBits(empfohlenerVerkaufspreis) != Double.doubleToLongBits(other.empfohlenerVerkaufspreis))
      return false;
    return true;
  }

}
