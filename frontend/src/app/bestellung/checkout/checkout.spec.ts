import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { Bestellung, Kunde, KundeUebersicht, Produkt, provideApi } from '../../api';
import { Warenkorb } from '../warenkorb';
import { Checkout } from './checkout';

const BUCH: Produkt = {
  id: 'p1',
  titel: 'Buch',
  listenpreis: { cent: 1250, waehrung: 'EUR' },
  lagerbestand: 10,
  istVerfuegbar: true,
};
const LAMPE: Produkt = {
  id: 'p2',
  titel: 'Lampe',
  listenpreis: { cent: 3000, waehrung: 'EUR' },
  lagerbestand: 1,
  istVerfuegbar: true,
};
const ANNA_UEBERSICHT: KundeUebersicht = {
  id: 'k1',
  vorname: 'Anna',
  nachname: 'Muster',
  email: 'anna@example.de',
};
const ANNA: Kunde = {
  ...ANNA_UEBERSICHT,
  adressen: [
    {
      id: 'a1',
      anschrift: { strasse: 'Ring 2', stadt: 'Wien', postleitzahl: '1010', land: 'AT' },
      istLieferadresse: false,
      istRechnungsadresse: true,
    },
    {
      id: 'a2',
      anschrift: { strasse: 'Hauptstr. 1', stadt: 'Berlin', postleitzahl: '10115', land: 'DE' },
      istLieferadresse: true,
      istRechnungsadresse: false,
    },
    {
      id: 'a3',
      anschrift: { strasse: 'Nebenstr. 5', stadt: 'Berlin', postleitzahl: '10115', land: 'DE' },
      istLieferadresse: true,
      istRechnungsadresse: false,
    },
  ],
};
const BESTELLUNG = { bestellnummer: 'B-2026-000123' } as Bestellung;

describe('Checkout', () => {
  let fixture: ComponentFixture<Checkout>;
  let http: HttpTestingController;
  let warenkorb: Warenkorb;
  let element: HTMLElement;

  const text = () => element.textContent ?? '';
  const alarm = () => element.querySelector('form [role="alert"]')?.textContent ?? '';

  function auswaehlen(id: string, wert: string): void {
    const feld = element.querySelector<HTMLSelectElement>(`#${id}`)!;
    feld.value = wert;
    feld.dispatchEvent(new Event('change'));
  }

  function eingeben(id: string, wert: string): void {
    const feld = element.querySelector<HTMLInputElement>(`#${id}`)!;
    feld.value = wert;
    feld.dispatchEvent(new Event('input'));
  }

  async function absenden(): Promise<void> {
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  async function erzeugen(): Promise<void> {
    fixture = TestBed.createComponent(Checkout);
    element = fixture.nativeElement;
    await fixture.whenStable();
  }

  async function kundeGewaehlt(): Promise<void> {
    http.expectOne('/api/v1/kunden').flush([ANNA_UEBERSICHT]);
    await fixture.whenStable();
    auswaehlen('kundenId', 'k1');
    http.expectOne('/api/v1/kunden/k1').flush(ANNA);
    await fixture.whenStable();
  }

  function fehler(status: number, title: string, detail: string) {
    return [
      { title, detail },
      { status, statusText: title },
    ] as const;
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [Checkout],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApi(''),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    warenkorb = TestBed.inject(Warenkorb);
  });

  afterEach(() => http.verify());

  describe('mit leerem Warenkorb', () => {
    beforeEach(async () => {
      await erzeugen();
      http.expectOne('/api/v1/kunden').flush([ANNA_UEBERSICHT]);
      await fixture.whenStable();
    });

    it('zeigt den Leerzustand ohne Kasse', () => {
      expect(text()).toContain('Der Warenkorb ist leer.');
      expect(element.querySelector('form')).toBeNull();
    });
  });

  describe('mit Buch und Lampe im Warenkorb', () => {
    beforeEach(async () => {
      warenkorb.lege(BUCH, 2);
      warenkorb.lege(LAMPE);
      await erzeugen();
    });

    it('zeigt Produkte mit Listenpreis und Menge, aber keine selbst berechneten Summen', async () => {
      await kundeGewaehlt();
      const zeilen = element.querySelectorAll('table tbody tr');

      expect(zeilen.length).toBe(2);
      expect(zeilen[0].textContent).toContain('Buch');
      expect(zeilen[0].textContent).toContain('12,50 €');
      expect(zeilen[0].querySelector<HTMLInputElement>('input')!.value).toBe('2');
      expect(text()).not.toContain('25,00 €');
      expect(text()).not.toContain('55,00 €');
    });

    it('ändert Mengen, verwirft ungültige und entfernt Produkte', async () => {
      await kundeGewaehlt();
      const menge = element.querySelector<HTMLInputElement>('[aria-label="Menge Buch"]')!;

      menge.value = '3';
      menge.dispatchEvent(new Event('change'));
      expect(warenkorb.mengeVon('p1')).toBe(3);

      menge.value = '0';
      menge.dispatchEvent(new Event('change'));
      expect(warenkorb.mengeVon('p1')).toBe(3);
      expect(menge.value).toBe('3');

      element.querySelector<HTMLButtonElement>('[aria-label="Lampe entfernen"]')!.click();
      await fixture.whenStable();
      expect(element.querySelectorAll('table tbody tr').length).toBe(1);
    });

    it('zeigt Laden und Fehler der Kundenliste und lädt erneut', async () => {
      expect(text()).toContain('Kunden werden geladen');
      http
        .expectOne('/api/v1/kunden')
        .flush(...fehler(500, 'Interner Fehler', 'Ein unerwarteter Fehler ist aufgetreten.'));
      await fixture.whenStable();
      expect(text()).toContain('Interner Fehler: Ein unerwarteter Fehler ist aufgetreten.');

      element.querySelector<HTMLButtonElement>('[role="alert"] button')!.click();
      http.expectOne('/api/v1/kunden').flush([ANNA_UEBERSICHT]);
      await fixture.whenStable();
      expect(element.querySelector('#kundenId')!.textContent).toContain('Anna Muster');
    });

    it('verweist ohne Kunden auf die Registrierung', async () => {
      http.expectOne('/api/v1/kunden').flush([]);
      await fixture.whenStable();
      expect(text()).toContain('Es gibt noch keine Kunden.');
      expect(element.querySelector('form')).toBeNull();
    });

    it('US-04.1 AK8: bietet nur Lieferadressen des gewählten Kunden an, die erste vorgewählt', async () => {
      await kundeGewaehlt();
      const optionen = element.querySelectorAll<HTMLInputElement>('input[type="radio"]');

      expect(optionen.length).toBe(2);
      expect(optionen[0].parentElement!.textContent).toContain('Hauptstr. 1, 10115 Berlin, DE');
      expect(optionen[0].checked).toBe(true);
      expect(text()).not.toContain('Ring 2');
    });

    it('zeigt einen Fehler beim Laden der Lieferadressen und lädt erneut', async () => {
      http.expectOne('/api/v1/kunden').flush([ANNA_UEBERSICHT]);
      await fixture.whenStable();
      auswaehlen('kundenId', 'k1');
      http
        .expectOne('/api/v1/kunden/k1')
        .flush(...fehler(404, 'Nicht gefunden', 'Kunde k1 existiert nicht'));
      await fixture.whenStable();
      expect(text()).toContain('Kunde k1 existiert nicht');

      element.querySelector<HTMLButtonElement>('fieldset [role="alert"] button')!.click();
      http.expectOne('/api/v1/kunden/k1').flush(ANNA);
      await fixture.whenStable();
      expect(element.querySelectorAll('input[type="radio"]').length).toBe(2);
    });

    it('lehnt ohne Kunde ab, ohne zu senden', async () => {
      http.expectOne('/api/v1/kunden').flush([ANNA_UEBERSICHT]);
      await fixture.whenStable();

      await absenden();

      expect(text()).toContain('Bitte einen Kunden wählen.');
      http.expectNone('/api/v1/kunden/k1/bestellungen');
    });

    it('US-04.1 AK1: sendet die Bestellung, leert den Warenkorb und zeigt die Bestätigung', async () => {
      const navigieren = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      await kundeGewaehlt();
      element.querySelectorAll<HTMLInputElement>('input[type="radio"]')[1].click();
      auswaehlen('zahlungsart', 'PAYPAL');
      eingeben('gutscheincode', ' herbst10 ');

      await absenden();
      const anfrage = http.expectOne('/api/v1/kunden/k1/bestellungen');
      expect(anfrage.request.method).toBe('POST');
      expect(anfrage.request.body).toEqual({
        positionen: [
          { produktId: 'p1', menge: 2 },
          { produktId: 'p2', menge: 1 },
        ],
        lieferadresseId: 'a3',
        zahlungsart: 'PAYPAL',
        gutscheincode: 'herbst10',
      });
      anfrage.flush(BESTELLUNG, { status: 201, statusText: 'Created' });
      await fixture.whenStable();

      expect(warenkorb.istLeer()).toBe(true);
      expect(navigieren).toHaveBeenCalledWith(['/kunden', 'k1', 'bestellungen', 'B-2026-000123'], {
        queryParams: { aufgegeben: 'ja' },
      });
    });

    it('US-08.1 AK4: zeigt den abgelaufenen Gutschein und bestellt ohne Code weiter', async () => {
      vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      await kundeGewaehlt();
      eingeben('gutscheincode', 'SOMMER20');

      await absenden();
      http
        .expectOne('/api/v1/kunden/k1/bestellungen')
        .flush(...fehler(422, 'Fachlich abgelehnt', 'Der Gutschein SOMMER20 ist abgelaufen.'));
      await fixture.whenStable();

      expect(alarm()).toContain('Der Gutschein SOMMER20 ist abgelaufen.');
      expect(warenkorb.eintraege().length).toBe(2);
      expect(element.querySelector<HTMLSelectElement>('#kundenId')!.value).toBe('k1');

      eingeben('gutscheincode', '');
      await absenden();
      const anfrage = http.expectOne('/api/v1/kunden/k1/bestellungen');
      expect(anfrage.request.body.gutscheincode).toBeUndefined();
      anfrage.flush(BESTELLUNG, { status: 201, statusText: 'Created' });
      await fixture.whenStable();
      expect(warenkorb.istLeer()).toBe(true);
    });

    it('US-08.1 AK7: ein zweiter Code ersetzt den ersten', async () => {
      vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      await kundeGewaehlt();
      eingeben('gutscheincode', 'HERBST10');
      eingeben('gutscheincode', 'MINUS15');

      await absenden();
      const anfrage = http.expectOne('/api/v1/kunden/k1/bestellungen');
      expect(anfrage.request.body.gutscheincode).toBe('MINUS15');
      anfrage.flush(BESTELLUNG, { status: 201, statusText: 'Created' });
    });

    it('US-08.1 AK6: zeigt einen unbekannten Gutschein', async () => {
      await kundeGewaehlt();
      eingeben('gutscheincode', 'GIBTSNICHT');

      await absenden();
      http
        .expectOne('/api/v1/kunden/k1/bestellungen')
        .flush(...fehler(422, 'Fachlich abgelehnt', 'Der Gutschein GIBTSNICHT ist unbekannt.'));
      await fixture.whenStable();

      expect(alarm()).toContain('Der Gutschein GIBTSNICHT ist unbekannt.');
    });

    it('US-04.1 AK3: zeigt fehlenden Bestand mit Produkt und behält den Warenkorb', async () => {
      await kundeGewaehlt();

      await absenden();
      http
        .expectOne('/api/v1/kunden/k1/bestellungen')
        .flush(
          ...fehler(409, 'Konflikt', 'Nicht genug Bestand für: Lampe (verfügbar 0, bestellt 1)'),
        );
      await fixture.whenStable();

      expect(alarm()).toContain('Nicht genug Bestand für: Lampe (verfügbar 0, bestellt 1)');
      expect(warenkorb.eintraege().length).toBe(2);
      const knopf = element.querySelector<HTMLButtonElement>('button[type="submit"]')!;
      expect(knopf.disabled).toBe(false);
    });
  });
});
