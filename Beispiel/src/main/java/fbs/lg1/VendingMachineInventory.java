package fbs.lg1;

import java.util.EnumMap;

public class VendingMachineInventory {
    EnumMap<Drink, Integer> stock = new EnumMap<>(Drink.class);
    EnumMap<Coin, Integer> coinRegister = new EnumMap<>(Coin.class);

    public void printStock() {
        System.out.println("Waren");
        for (Drink d : stock.keySet()) {
            int current = stock.get(d);
            String warning = current < d.minimumStock ? " (Mindestbestand unterschritten!)" : "";
            System.out.println(d.label + ": " + current + " Stück" + warning);
        }
    }

    public void printCoinRegister() {
        System.out.println("Münzen");
        for (Coin c : coinRegister.keySet()) {
            System.out.println(c.label + " (" + c.toEuroFormat() + "): " + coinRegister.get(c) + " Stück");
        }
    }
}
