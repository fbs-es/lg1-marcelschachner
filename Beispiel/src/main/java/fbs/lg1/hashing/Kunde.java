package fbs.lg1.hashing;

import java.util.Objects;

public class Kunde {
  String name;
  String vorname;
  Integer svnr;
  String betreuer;

  public Kunde(String name, String vorname, Integer svnr, String betreuer) {
    this.name = name;
    this.vorname = vorname;
    this.svnr = svnr;
    this.betreuer = betreuer;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, vorname, svnr, betreuer);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Kunde other = (Kunde) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (vorname == null) {
      if (other.vorname != null)
        return false;
    } else if (!vorname.equals(other.vorname))
      return false;
    if (svnr == null) {
      if (other.svnr != null)
        return false;
    } else if (!svnr.equals(other.svnr))
      return false;
    if (betreuer == null) {
      if (other.betreuer != null)
        return false;
    } else if (!betreuer.equals(other.betreuer))
      return false;
    return true;
  }

}
