import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Bestellung, Geld, provideApi } from '../../api';
import { BestellungDetail } from './bestellung-detail';

const euro = (cent: number): Geld => ({ cent, waehrung: 'EUR' });

const BESTELLUNG: Bestellung = {
  bestellnummer: 'B-2026-000123',
  kundenId: 'k1',
  bestelldatum: '2026-11-12',
  faelligAm: '2026-11-26',
  lieferanschrift: { strasse: 'Hauptstr. 1', stadt: 'Berlin', postleitzahl: '10115', land: 'DE' },
  positionen: [
    {
      zeilennummer: 1,
      produktId: 'p1',
      titel: 'Buch',
      menge: 2,
      einzelpreis: euro(1250),
      zwischenbetrag: euro(2500),
    },
    {
      zeilennummer: 2,
      produktId: 'p2',
      titel: 'Lampe',
      menge: 1,
      einzelpreis: euro(3000),
      zwischenbetrag: euro(3000),
    },
  ],
  warenwert: euro(5500),
  gutscheincode: 'HERBST10',
  rabattBetrag: euro(550),
  gesamtwert: euro(4950),
  zahlungen: [
    {
      id: 'z1',
      betrag: euro(4950),
      zahlungsart: 'RECHNUNG',
      buchungsart: 'ZAHLUNG',
      transaktionsstatus: 'AUSSTEHEND',
    },
  ],
};

describe('BestellungDetail', () => {
  let fixture: ComponentFixture<BestellungDetail>;
  let http: HttpTestingController;
  let element: HTMLElement;

  const URL = '/api/v1/kunden/k1/bestellungen/B-2026-000123';
  const text = () => element.textContent ?? '';

  async function erzeugen(aufgegeben?: string): Promise<void> {
    fixture = TestBed.createComponent(BestellungDetail);
    fixture.componentRef.setInput('kundenId', 'k1');
    fixture.componentRef.setInput('bestellnummer', 'B-2026-000123');
    fixture.componentRef.setInput('aufgegeben', aufgegeben);
    element = fixture.nativeElement;
    await fixture.whenStable();
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [BestellungDetail],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApi(''),
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('US-04.1 AK1, US-08.1 AK1: zeigt die Bestellung mit Werten vom Backend', async () => {
    await erzeugen();
    expect(text()).toContain('Bestellung wird geladen');
    http.expectOne(URL).flush(BESTELLUNG);
    await fixture.whenStable();

    expect(element.querySelector('h1')!.textContent).toContain('B-2026-000123');
    expect(text()).toContain('12.11.2026');
    expect(text()).toContain('26.11.2026');
    expect(text()).toContain('Hauptstr. 1');
    const zeilen = element.querySelectorAll('tbody tr');
    expect(zeilen[0].textContent).toContain('Buch');
    expect(zeilen[0].textContent).toContain('12,50 €');
    expect(zeilen[0].textContent).toContain('25,00 €');
    const summen = element.querySelector('tfoot')!.textContent!;
    expect(summen).toContain('55,00 €');
    expect(summen).toContain('Rabatt (Gutschein HERBST10)');
    expect(summen).toContain('−5,50 €');
    expect(element.querySelector('.gesamt')!.textContent).toContain('49,50 €');
    expect(element.querySelector('.zahlungen')!.textContent).toContain(
      'Rechnung: 49,50 €, ausstehend',
    );
    expect(text()).not.toContain('wurde aufgegeben');
  });

  it('zeigt ohne Gutschein keine Rabattzeile', async () => {
    await erzeugen();
    http.expectOne(URL).flush({ ...BESTELLUNG, gutscheincode: undefined, rabattBetrag: euro(0) });
    await fixture.whenStable();

    expect(element.querySelector('.rabatt')).toBeNull();
  });

  it('zeigt nach dem Checkout die Bestätigung', async () => {
    await erzeugen('ja');
    http.expectOne(URL).flush(BESTELLUNG);
    await fixture.whenStable();

    expect(element.querySelector('.erfolg')!.textContent).toContain(
      'Die Bestellung B-2026-000123 wurde aufgegeben.',
    );
  });

  it('zeigt eine unbekannte Bestellung als Fehler und lädt erneut', async () => {
    await erzeugen();
    http
      .expectOne(URL)
      .flush(
        { title: 'Nicht gefunden', detail: 'Bestellung B-2026-000123 existiert nicht' },
        { status: 404, statusText: 'Not Found' },
      );
    await fixture.whenStable();
    expect(element.querySelector('[role="alert"]')!.textContent).toContain(
      'Bestellung B-2026-000123 existiert nicht',
    );

    element.querySelector<HTMLButtonElement>('[role="alert"] button')!.click();
    http.expectOne(URL).flush(BESTELLUNG);
    await fixture.whenStable();
    expect(element.querySelector('h1')!.textContent).toContain('B-2026-000123');
  });
});
