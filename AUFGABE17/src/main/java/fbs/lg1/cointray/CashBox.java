package fbs.lg1.cointray;

import java.util.EnumMap;
import java.util.Map;

public class CashBox {

    private final Map<Muenzen, Integer> coins;

    public CashBox() {
        coins = new EnumMap<>(Muenzen.class);
        for (Muenzen coin : Muenzen.values()) {
            coins.put(coin, 0);
        }
    }

    public int getCoinCount(Muenzen coin) {
        return coins.get(coin);
    }

    public void setCoinCount(Muenzen coin, int count) {
        coins.put(coin, count);
    }

    @Deprecated
    public boolean canGiveChange(int amountInCents) {
        int remaining = amountInCents;
        for (Muenzen coin : Muenzen.values()) {
            int available = coins.get(coin);
            int needed = remaining / coin.getValueInCents();
            int used = Math.min(needed, available);
            remaining -= used * coin.getValueInCents();
        }
        return remaining == 0;
    }

    @Deprecated
    public Map<Muenzen, Integer> payOutChange(int amountInCents) {
        Map<Muenzen, Integer> change = new EnumMap<>(Muenzen.class);
        int remaining = amountInCents;
        for (Muenzen coin : Muenzen.values()) {
            int available = coins.get(coin);
            int needed = remaining / coin.getValueInCents();
            int used = Math.min(needed, available);
            if (used > 0) {
                change.put(coin, used);
                remaining -= used * coin.getValueInCents();
            }
        }
        if (remaining == 0) {
            for (Map.Entry<Muenzen, Integer> entry : change.entrySet()) {
                coins.put(entry.getKey(), coins.get(entry.getKey()) - entry.getValue());
            }
            return change;
        }
        return null;
    }

    public int berechneKassenstand() {
        int sum = 0;
        for (Map.Entry<Muenzen, Integer> entry : coins.entrySet()) {
            sum += entry.getKey().getValueInCents() * entry.getValue();
        }
        return sum;
    }

    public boolean kannWechseln(int betrag, Map<Muenzen, Integer> einwurf) {
        Map<Muenzen, Integer> combined = new EnumMap<>(coins);
        for (Map.Entry<Muenzen, Integer> entry : einwurf.entrySet()) {
            combined.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        int remaining = betrag;
        for (Muenzen coin : Muenzen.values()) {
            int available = combined.getOrDefault(coin, 0);
            int needed = remaining / coin.getValueInCents();
            int used = Math.min(needed, available);
            remaining -= used * coin.getValueInCents();
        }
        return remaining == 0;
    }

    public Map<Muenzen, Integer> gebeWechselgeld(int betrag, Map<Muenzen, Integer> einwurf) {
        if (!kannWechseln(betrag, einwurf)) {
            return null;
        }
        Map<Muenzen, Integer> change = berechneWechselmuenzen(betrag, einwurf);
        reduzierKassette(change);
        return change;
    }

    private Map<Muenzen, Integer> berechneWechselmuenzen(int betrag, Map<Muenzen, Integer> einwurf) {
        Map<Muenzen, Integer> change = new EnumMap<>(Muenzen.class);
        Map<Muenzen, Integer> combined = new EnumMap<>(coins);
        einwurf.forEach((k, v) -> combined.merge(k, v, Integer::sum));
        int remaining = betrag;
        for (Muenzen coin : Muenzen.values()) {
            int available = combined.getOrDefault(coin, 0);
            int needed = remaining / coin.getValueInCents();
            int used = Math.min(needed, available);
            if (used > 0) {
                change.put(coin, used);
                remaining -= used * coin.getValueInCents();
            }
        }
        return change;
    }

    private void reduzierKassette(Map<Muenzen, Integer> change) {
        change.forEach((k, v) -> coins.merge(k, -v, Integer::sum));
    }
}
