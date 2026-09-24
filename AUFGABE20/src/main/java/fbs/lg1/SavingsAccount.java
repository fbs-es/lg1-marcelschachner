package fbs.lg1;

import java.math.BigDecimal;

public class SavingsAccount {

    private static int nextAccountNumber = 1;

    private final int accountNumber;
    private final String iban;
    private String ownerName;
    private BigDecimal balance;

    public SavingsAccount(String ownerName, BigDecimal initialBalance) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }
        if (initialBalance == null) {
            throw new IllegalArgumentException("Startguthaben darf nicht fehlen!");
        }
        if (initialBalance.signum() < 0) {
            initialBalance = BigDecimal.ZERO;
        }

        this.ownerName = ownerName;
        this.balance = initialBalance;
        this.accountNumber = nextAccountNumber++;
        this.iban = "AT00" + "12345" + String.format("%011d", accountNumber);

        checkInvariant();
    }

    public void deposit(BigDecimal amount) {
        checkAmount(amount);

        BigDecimal oldBalance = this.balance;

        this.balance = this.balance.add(amount);

        assert this.balance.compareTo(oldBalance.add(amount)) == 0
                : "Fehler: Kontostand wurde nicht korrekt erhöht!";
        checkInvariant();
    }

    public void withdraw(BigDecimal amount) {
        checkAmount(amount);
        checkCoverage(amount);

        BigDecimal oldBalance = this.balance;

        this.balance = this.balance.subtract(amount);

        assert this.balance.compareTo(oldBalance.subtract(amount)) == 0
                : "Fehler: Kontostand wurde nicht korrekt verringert!";
        checkInvariant();
    }

    public void transferTo(SavingsAccount target, BigDecimal amount) {
        if (target == null) {
            throw new IllegalArgumentException("Zielkonto existiert nicht!");
        }
        if (target == this) {
            throw new IllegalArgumentException("Überweisung auf dasselbe Konto nicht erlaubt!");
        }

        BigDecimal oldSum = this.balance.add(target.balance);

        this.withdraw(amount);
        target.deposit(amount);

        assert this.balance.add(target.balance).compareTo(oldSum) == 0
                : "Fehler: Summe der Kontostände hat sich verändert!";
    }

    public void changeOwnerName(String newName) {

        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }

        this.ownerName = newName;

        assert this.ownerName.equals(newName) : "Fehler: Name wurde nicht korrekt geändert!";
        checkInvariant();
    }

    private void checkAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Betrag darf nicht fehlen!");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Betrag muss positiv sein!");
        }
    }

    private void checkCoverage(BigDecimal amount) {
        if (amount.compareTo(this.balance) > 0) {
            throw new IllegalStateException("Nicht genügend Guthaben vorhanden!");
        }
    }

    private void checkInvariant() {
        if (balance == null || balance.signum() < 0) {
            throw new IllegalStateException("Invariante verletzt: Kontostand ist ungültig!");
        }
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalStateException(
                    "Invariante verletzt: Name des Kontoinhabers ist ungültig!");
        }
    }

    public String ownerName() {
        return ownerName;
    }

    public BigDecimal balance() {
        return balance;
    }

    public int accountNumber() {
        return accountNumber;
    }

    public String iban() {
        return iban;
    }
}
