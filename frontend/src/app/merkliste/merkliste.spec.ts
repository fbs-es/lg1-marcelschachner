import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Merklisteneintrag, Produkt, provideApi } from '../api';
import { Merkliste } from './merkliste';

const MERKLISTE = '/api/v1/kunden/k1/merkliste';
const PRODUKTE = '/api/v1/produkte';

const KABEL: Merklisteneintrag = {
  produktId: 'p-kabel',
  titel: 'Kabel',
  listenpreis: { cent: 999, waehrung: 'EUR' },
  istVerfuegbar: false,
  hinzugefuegtAm: '2026-11-15',
};
const KOPFHOERER: Merklisteneintrag = {
  produktId: 'p-kopf',
  titel: 'Kopfhörer',
  listenpreis: { cent: 7999, waehrung: 'EUR' },
  istVerfuegbar: true,
  hinzugefuegtAm: '2026-11-12',
};
const PRODUKT_LISTE: Produkt[] = [
  {
    id: 'p-kabel',
    titel: 'Kabel',
    listenpreis: { cent: 999, waehrung: 'EUR' },
    lagerbestand: 0,
    istVerfuegbar: false,
  },
  {
    id: 'p-kopf',
    titel: 'Kopfhörer',
    listenpreis: { cent: 7999, waehrung: 'EUR' },
    lagerbestand: 3,
    istVerfuegbar: true,
  },
];

describe('Merkliste', () => {
  let fixture: ComponentFixture<Merkliste>;
  let http: HttpTestingController;
  let element: HTMLElement;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [Merkliste],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApi(''),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Merkliste);
    fixture.componentRef.setInput('kundenId', 'k1');
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  async function geladen(eintraege: Merklisteneintrag[]): Promise<void> {
    http.expectOne(MERKLISTE).flush(eintraege);
    http.expectOne(PRODUKTE).flush(PRODUKT_LISTE);
    await fixture.whenStable();
  }

  it('US-03.1 AK3: zeigt Einträge in Backend-Reihenfolge mit Datum, Preis und Verfügbarkeit', async () => {
    expect(element.textContent).toContain('Merkliste wird geladen');
    await geladen([KABEL, KOPFHOERER]);

    const zeilen = element.querySelectorAll('tbody tr');
    expect(zeilen.length).toBe(2);
    expect(zeilen[0].textContent).toContain('Kabel');
    expect(zeilen[0].textContent).toContain('15.11.2026');
    expect(zeilen[0].textContent).toContain('9,99 €');
    expect(zeilen[0].textContent).toContain('nicht verfügbar');
    expect(zeilen[1].textContent).toContain('Kopfhörer');
    expect(zeilen[1].textContent).toContain('12.11.2026');
    expect(zeilen[1].textContent).toContain('79,99 €');
    expect(zeilen[1].textContent).toContain('verfügbar');
  });

  it('US-03.1 AK5: zeigt Leerzustand ohne Fehler', async () => {
    await geladen([]);

    expect(element.textContent).toContain('Noch nichts gemerkt.');
    expect(element.querySelector('table')).toBeNull();
  });

  it('US-03.1 AK1: merkt das gewählte Produkt und lädt die Liste neu', async () => {
    await geladen([]);

    const auswahl = element.querySelector<HTMLSelectElement>('select#produktId')!;
    auswahl.value = auswahl.options[2].value;
    auswahl.dispatchEvent(new Event('change'));
    element.querySelector<HTMLButtonElement>('form button[type="submit"]')!.click();

    const anfrage = http.expectOne(`${MERKLISTE}/p-kopf`);
    expect(anfrage.request.method).toBe('PUT');
    anfrage.flush(KOPFHOERER, { status: 201, statusText: 'Created' });
    http.expectOne(MERKLISTE).flush([KOPFHOERER]);
    await fixture.whenStable();

    expect(element.querySelectorAll('tbody tr').length).toBe(1);
    expect(element.querySelector('tbody')!.textContent).toContain('Kopfhörer');
  });

  it('merken ohne Auswahl sendet nichts', async () => {
    await geladen([]);

    element.querySelector<HTMLButtonElement>('form button[type="submit"]')!.click();
    await fixture.whenStable();

    http.expectNone(`${MERKLISTE}/p-kopf`);
    expect(element.textContent).toContain('Bitte ein Produkt wählen.');
  });

  it('US-03.2 AK1: entfernt einen Eintrag und lädt die Liste neu', async () => {
    await geladen([KABEL, KOPFHOERER]);

    element.querySelectorAll<HTMLButtonElement>('tbody button')[1].click();

    const anfrage = http.expectOne(`${MERKLISTE}/p-kopf`);
    expect(anfrage.request.method).toBe('DELETE');
    anfrage.flush(null, { status: 204, statusText: 'No Content' });
    http.expectOne(MERKLISTE).flush([KABEL]);
    await fixture.whenStable();

    const zeilen = element.querySelectorAll('tbody tr');
    expect(zeilen.length).toBe(1);
    expect(zeilen[0].textContent).toContain('Kabel');
  });

  it('zeigt Fehler einer Aktion verständlich, Liste bleibt', async () => {
    await geladen([KABEL]);

    element.querySelector<HTMLButtonElement>('tbody button')!.click();
    http
      .expectOne(`${MERKLISTE}/p-kabel`)
      .flush(
        { title: 'Nicht gefunden', detail: 'Kunde k1 existiert nicht' },
        { status: 404, statusText: 'Not Found' },
      );
    await fixture.whenStable();

    expect(element.querySelector('[role="alert"]')!.textContent).toContain(
      'Nicht gefunden: Kunde k1 existiert nicht',
    );
    expect(element.querySelectorAll('tbody tr').length).toBe(1);
  });

  it('zeigt Ladefehler verständlich und lädt erneut', async () => {
    http
      .expectOne(MERKLISTE)
      .flush(
        { title: 'Nicht gefunden', detail: 'Kunde k1 existiert nicht' },
        { status: 404, statusText: 'Not Found' },
      );
    expect(http.expectOne(PRODUKTE).cancelled).toBe(true);
    await fixture.whenStable();
    expect(element.querySelector('[role="alert"]')!.textContent).toContain(
      'Nicht gefunden: Kunde k1 existiert nicht',
    );

    element.querySelector<HTMLButtonElement>('[role="alert"] button')!.click();
    await geladen([KABEL]);
    expect(element.querySelectorAll('tbody tr').length).toBe(1);
  });
});
