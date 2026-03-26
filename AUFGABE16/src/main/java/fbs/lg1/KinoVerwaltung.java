package fbs.lg1;

public class KinoVerwaltung {

    private static final int REIHEN = 13;
    private static final int PLAETZE = 9;
    private final Kinosaal saal;

    public KinoVerwaltung() {
        this(REIHEN, PLAETZE);
    }

    public KinoVerwaltung(int reihen, int plaetze) {
        saal = new Kinosaal(reihen, plaetze);
    }

    public int getReihen() {
        return saal.getReihen();
    }

    public int getPlaetze() {
        return saal.getPlaetzeProReihe();
    }

    public SitzplatzStatus getSitzplatzStatus(int reihe, int platz) {
        return saal.getStatus(reihe, platz);
    }

    public int zaehle(SitzplatzStatus status) {
        int count = 0;
        for (int i = 0; i < saal.getReihen(); i++) {
            for (int j = 0; j < saal.getPlaetzeProReihe(); j++) {
                if (saal.getStatus(i, j) == status)
                    count++;
            }
        }
        return count;
    }

    public int[][] vormerken(int anzahl) {
        return vormerken(anzahl, 0, 0);
    }

    public int[][] vormerken(int anzahl, int startReihe, int startSitz) {
        if (anzahl < 1 || anzahl > 3) {
            return new int[0][0];
        }
        for (int i = startReihe; i < saal.getReihen(); i++) {
            for (int j = (i == startReihe ? startSitz : 0); j <= saal.getPlaetzeProReihe() - anzahl; j++) {
                boolean alleFrei = true;
                for (int k = 0; k < anzahl; k++) {
                    if (saal.getStatus(i, j + k) != SitzplatzStatus.FREI) {
                        alleFrei = false;
                        break;
                    }
                }
                if (alleFrei) {
                    int[][] positionen = new int[anzahl][2];
                    for (int k = 0; k < anzahl; k++) {
                        saal.setStatus(i, j + k, SitzplatzStatus.VORGEMERKT);
                        positionen[k][0] = i;
                        positionen[k][1] = j + k;
                    }
                    return positionen;
                }
            }
        }
        return new int[0][0];
    }

    public void bestaetigen(int[][] positionen) {
        updateVorgemerkt(positionen, SitzplatzStatus.RESERVIERT);
    }

    public void ablehnen(int[][] positionen) {
        updateVorgemerkt(positionen, SitzplatzStatus.FREI);
    }

    private void updateVorgemerkt(int[][] positionen, SitzplatzStatus ziel) {
        for (int[] pos : positionen) {
            if (saal.getStatus(pos[0], pos[1]) == SitzplatzStatus.VORGEMERKT) {
                saal.setStatus(pos[0], pos[1], ziel);
            }
        }
    }
}
