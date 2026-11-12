import { TestBed } from '@angular/core/testing';

import { Produkt } from '../api';
import { Warenkorb } from './warenkorb';

const BUCH: Produkt = {
  id: 'p1',
  titel: 'Buch',
  listenpreis: { cent: 1250, waehrung: 'EUR' },
  lagerbestand: 10,
  istVerfuegbar: true,
};
const LAMPE: Produkt = {
  ...BUCH,
  id: 'p2',
  titel: 'Lampe',
  listenpreis: { cent: 3000, waehrung: 'EUR' },
};

describe('Warenkorb', () => {
  let warenkorb: Warenkorb;

  beforeEach(() => {
    warenkorb = TestBed.inject(Warenkorb);
  });

  it('ist anfangs leer', () => {
    expect(warenkorb.istLeer()).toBe(true);
    expect(warenkorb.stueckzahl()).toBe(0);
  });

  it('legt Produkte in Reihenfolge des Hinzufügens ab', () => {
    warenkorb.lege(BUCH, 2);
    warenkorb.lege(LAMPE);

    expect(warenkorb.eintraege().map((e) => [e.produkt.titel, e.menge])).toEqual([
      ['Buch', 2],
      ['Lampe', 1],
    ]);
    expect(warenkorb.stueckzahl()).toBe(3);
  });

  it('fasst dasselbe Produkt zu einem Eintrag zusammen', () => {
    warenkorb.lege(BUCH);
    warenkorb.lege(BUCH);

    expect(warenkorb.eintraege().length).toBe(1);
    expect(warenkorb.mengeVon('p1')).toBe(2);
  });

  it('ändert die Menge nur auf ganze Zahlen ab 1', () => {
    warenkorb.lege(BUCH);

    warenkorb.aendereMenge('p1', 4);
    expect(warenkorb.mengeVon('p1')).toBe(4);

    warenkorb.aendereMenge('p1', 0);
    warenkorb.aendereMenge('p1', 1.5);
    warenkorb.aendereMenge('p1', Number.NaN);
    expect(warenkorb.mengeVon('p1')).toBe(4);
  });

  it('entfernt ein Produkt und leert den Warenkorb', () => {
    warenkorb.lege(BUCH);
    warenkorb.lege(LAMPE);

    warenkorb.entferne('p1');
    expect(warenkorb.eintraege().map((e) => e.produkt.id)).toEqual(['p2']);

    warenkorb.leere();
    expect(warenkorb.istLeer()).toBe(true);
  });
});
