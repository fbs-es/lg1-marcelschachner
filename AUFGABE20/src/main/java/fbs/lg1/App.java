package fbs.lg1;

import java.math.BigDecimal;

public class App {
    public static void main(String[] args) {
        SavingsAccount account = new SavingsAccount("Marcel Schachner", new BigDecimal("100.00"));
        SavingsAccount other = new SavingsAccount("Marcel Schachner", BigDecimal.ZERO);
        System.out.println("Konto von " + account.ownerName() + " (IBAN " + account.iban() + ")");

        account.deposit(new BigDecimal("50.00"));
        account.withdraw(new BigDecimal("30.00"));
        account.changeOwnerName("Marcel Ferdinand Schachner");
        account.transferTo(other, new BigDecimal("20.00"));

        try {
            account.deposit(new BigDecimal("-10.00"));
        } catch (IllegalArgumentException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        try {
            account.withdraw(new BigDecimal("500.00"));
        } catch (IllegalStateException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        try {
            account.changeOwnerName("   ");
        } catch (IllegalArgumentException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        try {
            account.transferTo(account, new BigDecimal("20.00"));
        } catch (IllegalArgumentException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        System.out.println("Inhaber: " + account.ownerName());
        System.out.println("Kontostand: " + account.balance() + " EUR");
        System.out.println("Kontostand " + other.ownerName() + ": " + other.balance() + " EUR");
    }
}
