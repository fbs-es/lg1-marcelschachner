// wird Interface
package fbs.lg1.inventory;

import java.util.EnumMap;
import java.util.Map;

import fbs.lg1.Drinks;
import fbs.lg1.cointray.CashBox;
import fbs.lg1.cointray.Muenzen;

public class Inventory {

    private final Stock stock;
    private final CashBox cashBox;

    public Inventory() {
        stock = new Stock();
        cashBox = new CashBox();
    }

    public int getStockQuantity(Drinks drink) {
        return stock.getQuantity(drink);
    }

    public void setStockQuantity(Drinks drink, int quantity) {
        stock.setQuantity(drink, quantity);
    }

    public void dispense(Drinks drink) {
        stock.dispense(drink);
    }

    public EnumMap<Drinks, Integer> pruefeNachfuellbedarf() {
        return stock.pruefeNachfuellbedarf();
    }

    public EnumMap<Drinks, Integer> ermittleBedarf() {
        return stock.ermittleBedarf();
    }

    public void auffuellen(Map<Drinks, Integer> lieferung) {
        stock.auffuellen(lieferung);
    }

    public int getCoinCount(Muenzen coin) {
        return cashBox.getCoinCount(coin);
    }

    public void setCoinCount(Muenzen coin, int count) {
        cashBox.setCoinCount(coin, count);
    }

    public int berechneKassenstand() {
        return cashBox.berechneKassenstand();
    }

    public boolean kannWechseln(int betrag, Map<Muenzen, Integer> einwurf) {
        return cashBox.kannWechseln(betrag, einwurf);
    }

    public Map<Muenzen, Integer> gebeWechselgeld(int betrag, Map<Muenzen, Integer> einwurf) {
        return cashBox.gebeWechselgeld(betrag, einwurf);
    }
}
