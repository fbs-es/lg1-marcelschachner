package fbs.lg1;

public class Kinosaal {

    private final SitzplatzStatus[][] plaetze;

    public Kinosaal(int reihen, int plaetzeProReihe) {
        plaetze = new SitzplatzStatus[reihen][plaetzeProReihe];
        init();
    }

    private void init() {
        for (int i = 0; i < plaetze.length; i++) {
            for (int j = 0; j < plaetze[i].length; j++) {
                plaetze[i][j] = (i == plaetze.length - 1) ? SitzplatzStatus.RESERVIERT : SitzplatzStatus.FREI;
            }
        }
    }

    public SitzplatzStatus getStatus(int reihe, int platz) {
        return plaetze[reihe][platz];
    }

    public void setStatus(int reihe, int platz, SitzplatzStatus status) {
        plaetze[reihe][platz] = status;
    }

    public int getReihen() {
        return plaetze.length;
    }

    public int getPlaetzeProReihe() {
        return plaetze[0].length;
    }

}
