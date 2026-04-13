package fbs.lg1.inventory;

import java.util.EnumMap;
import java.util.Map;

import fbs.lg1.Drinks;

public class Stock {

    private final EnumMap<Drinks, Integer> drinks;

    public Stock() {
        drinks = new EnumMap<>(Drinks.class);
        for (Drinks drink : Drinks.values()) {
            drinks.put(drink, 0);
        }
    }

    public int getQuantity(Drinks drink) {
        return drinks.get(drink);
    }

    public void setQuantity(Drinks drink, int quantity) {
        drinks.put(drink, quantity);
    }

    public void dispense(Drinks drink) {
        drinks.put(drink, drinks.get(drink) - 1);
    }

    // TODO foreach
    public EnumMap<Drinks, Integer> pruefeNachfuellbedarf() {
        EnumMap<Drinks, Integer> liste = new EnumMap<>(Drinks.class);
        for (Drinks drink : Drinks.values()) {
            if (drinks.get(drink) < drink.getMinimumStock()) {
                liste.put(drink, drinks.get(drink));
            }
        }
        return liste;
    }

    public EnumMap<Drinks, Integer> ermittleBedarf() {
        EnumMap<Drinks, Integer> bedarf = new EnumMap<>(Drinks.class);
        for (Drinks drink : Drinks.values()) {
            if (drinks.get(drink) < drink.getMinimumStock()) {
                bedarf.put(drink, drink.getMaximumStock() - drinks.get(drink));
            }
        }
        return bedarf;
    }

    // TODO foreach wie bei Münzen
    public void auffuellen(Map<Drinks, Integer> lieferung) {
        for (Map.Entry<Drinks, Integer> entry : lieferung.entrySet()) {
            Drinks drink = entry.getKey();
            int neu = drinks.get(drink) + entry.getValue();
            drinks.put(drink, Math.min(neu, drink.getMaximumStock()));
        }
    }
}
