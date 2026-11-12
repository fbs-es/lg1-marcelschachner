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
  bezahlteSumme: euro(0),
  offenerBetrag: euro(4950),
  istVollstaendigBezahlt: false,
  pakete: [],
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

  it('US-05.1 AK1: zeigt offenen Bezahlstatus und offenen Betrag vom Backend', async () => {
    await erzeugen();
    http.expectOne(URL).flush(BESTELLUNG);
    await fixture.whenStable();

    expect(element.querySelector('.bezahlstatus .status')!.textContent).toContain('offen');
    expect(element.querySelector('.bezahlstatus .status')!.classList).not.toContain('bezahlt');
    expect(element.querySelector('.bezahlt-summe')!.textContent).toContain('0,00 €');
    expect(element.querySelector('.offen')!.textContent).toContain('49,50 €');
  });

  it('US-05.1 AK5: zeigt Erstattung, bezahlte Summe und Restbetrag', async () => {
    await erzeugen();
    http.expectOne(URL).flush({
      ...BESTELLUNG,
      zahlungen: [
        { ...BESTELLUNG.zahlungen[0], transaktionsstatus: 'ERFOLGREICH' },
        {
          id: 'z2',
          betrag: euro(1000),
          zahlungsart: 'RECHNUNG',
          buchungsart: 'ERSTATTUNG',
          transaktionsstatus: 'ERFOLGREICH',
        },
      ],
      bezahlteSumme: euro(3950),
      offenerBetrag: euro(1000),
    } satisfies Bestellung);
    await fixture.whenStable();

    const zahlungen = element.querySelectorAll('.zahlungen li');
    expect(zahlungen[0].textContent).toContain('Rechnung: 49,50 €, erfolgreich');
    expect(zahlungen[1].textContent!.replace(/[ \n]+/g, ' ')).toContain(
      'Erstattung Rechnung: 10,00 €, erfolgreich',
    );
    expect(element.querySelector('.bezahlt-summe')!.textContent).toContain('39,50 €');
    expect(element.querySelector('.offen')!.textContent).toContain('10,00 €');
  });

  it('US-05.1 AK2: zeigt vollständig bezahlt', async () => {
    await erzeugen();
    http.expectOne(URL).flush({
      ...BESTELLUNG,
      zahlungen: [{ ...BESTELLUNG.zahlungen[0], transaktionsstatus: 'ERFOLGREICH' }],
      bezahlteSumme: euro(4950),
      offenerBetrag: euro(0),
      istVollstaendigBezahlt: true,
    } satisfies Bestellung);
    await fixture.whenStable();

    const status = element.querySelector('.bezahlstatus .status')!;
    expect(status.textContent).toContain('vollständig bezahlt');
    expect(status.classList).toContain('bezahlt');
    expect(element.querySelector('.offen')!.textContent).toContain('0,00 €');
  });

  it('US-07.3 AK1/AK2: zeigt den Versandabschnitt mit den Paketen vom Backend', async () => {
    await erzeugen();
    http.expectOne(URL).flush(BESTELLUNG);
    await fixture.whenStable();
    expect(element.querySelector('.keine-pakete')!.textContent).toContain(
      'Noch kein Paket versandt.',
    );

    await erzeugen();
    http.expectOne(URL).flush({
      ...BESTELLUNG,
      pakete: [
        {
          id: 'v1',
          dienstleister: 'DHL',
          sendungsnummer: 'AB123DE',
          lieferstatus: 'IN_ZUSTELLUNG',
          angelegtAm: '2026-11-12T09:30:00Z',
          statusGeaendertAm: '2026-11-13T08:00:00Z',
          erlaubteFolgestatus: ['ZUGESTELLT', 'RETOURE', 'VERLOREN'],
        },
      ],
    } satisfies Bestellung);
    await fixture.whenStable();
    expect(element.querySelector('.pakete')!.textContent).toContain('AB123DE');
    expect(element.querySelector('.pakete')!.textContent).toContain('in Zustellung');
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
