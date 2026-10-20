package fbs.lg1;

import fbs.payment.PaymentService;

class FakePaymentService implements PaymentService {

    private int anzahlStornierungen;
    private double stornierterBetrag;
    private String email;

    @Override
    public void bezahlen(int bestellnummer, double betrag, String email) {
        this.email = email;
    }

    @Override
    public void stornieren(int bestellnummer, double betrag, String email) {
        anzahlStornierungen++;
        stornierterBetrag = betrag;
        this.email = email;
    }

    int showAnzahlStornierungen() {
        return anzahlStornierungen;
    }

    double showStornierterBetrag() {
        return stornierterBetrag;
    }

    String showEmail() {
        return email;
    }
}
