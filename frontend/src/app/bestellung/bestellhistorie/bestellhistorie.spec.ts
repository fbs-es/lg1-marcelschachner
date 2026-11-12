import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { BestellungUebersicht, provideApi } from '../../api';
import { Bestellhistorie } from './bestellhistorie';

const URL = '/api/v1/kunden/k1/bestellungen';
const NEUESTE: BestellungUebersicht = {
  bestellnummer: 'B-2026-000003',
  bestelldatum: '2026-11-12',
  anzahlPositionen: 1,
  gesamtwert: { cent: 1250, waehrung: 'EUR' },
};
const MIT_GUTSCHEIN: BestellungUebersicht = {
  bestellnummer: 'B-2026-000002',
  bestelldatum: '2026-11-12',
  anzahlPositionen: 2,
  gesamtwert: { cent: 4950, waehrung: 'EUR' },
};

describe('Bestellhistorie', () => {
  let fixture: ComponentFixture<Bestellhistorie>;
  let http: HttpTestingController;
  let element: HTMLElement;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [Bestellhistorie],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApi(''),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Bestellhistorie);
    fixture.componentRef.setInput('kundenId', 'k1');
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('US-04.2 AK1, AK2: zeigt die Bestellungen in der Reihenfolge des Backends', async () => {
    expect(element.textContent).toContain('Bestellungen werden geladen');
    http.expectOne(URL).flush([NEUESTE, MIT_GUTSCHEIN]);
    await fixture.whenStable();

    const zeilen = element.querySelectorAll('tbody tr');
    expect(zeilen.length).toBe(2);
    expect(zeilen[0].textContent).toContain('B-2026-000003');
    expect(zeilen[1].textContent).toContain('B-2026-000002');
    expect(zeilen[1].textContent).toContain('12.11.2026');
    expect(zeilen[1].textContent).toContain('2');
    expect(zeilen[1].textContent).toContain('49,50 €');
  });

  it('US-04.2 AK7: verlinkt jede Bestellung auf ihre Ansicht', async () => {
    http.expectOne(URL).flush([MIT_GUTSCHEIN]);
    await fixture.whenStable();

    expect(element.querySelector('tbody a')!.getAttribute('href')).toBe(
      '/kunden/k1/bestellungen/B-2026-000002',
    );
  });

  it('US-04.2 AK5: zeigt "Noch keine Bestellungen" ohne Bestellungen', async () => {
    http.expectOne(URL).flush([]);
    await fixture.whenStable();

    expect(element.textContent).toContain('Noch keine Bestellungen.');
    expect(element.querySelector('table')).toBeNull();
  });

  it('zeigt Fehler verständlich und lädt erneut', async () => {
    http
      .expectOne(URL)
      .flush(
        { title: 'Nicht gefunden', detail: 'Kunde k1 existiert nicht' },
        { status: 404, statusText: 'Not Found' },
      );
    await fixture.whenStable();
    expect(element.querySelector('[role="alert"]')!.textContent).toContain(
      'Nicht gefunden: Kunde k1 existiert nicht',
    );

    element.querySelector<HTMLButtonElement>('[role="alert"] button')!.click();
    http.expectOne(URL).flush([NEUESTE]);
    await fixture.whenStable();
    expect(element.querySelectorAll('tbody tr').length).toBe(1);
  });
});
