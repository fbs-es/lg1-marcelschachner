import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localeDe from '@angular/common/locales/de';
import { LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Gutschein, provideApi } from '../../api';
import { Gutscheine } from './gutscheine';

registerLocaleData(localeDe);

const HERBST10: Gutschein = {
  id: 'g1',
  code: 'HERBST10',
  rabattArt: 'PROZENT',
  prozent: 12.5,
  ablaufdatum: '2026-12-31',
  istGueltig: true,
};

const MINUS5: Gutschein = {
  id: 'g2',
  code: 'MINUS5',
  rabattArt: 'FESTBETRAG',
  festbetrag: { cent: 500, waehrung: 'EUR' },
  ablaufdatum: '2026-08-31',
  istGueltig: false,
};

describe('Gutscheine (US-08.2)', () => {
  let fixture: ComponentFixture<Gutscheine>;
  let http: HttpTestingController;
  let element: HTMLElement;

  function eingeben(id: string, wert: string): void {
    const feld = element.querySelector<HTMLInputElement | HTMLSelectElement>(`#${id}`)!;
    feld.value = wert;
    feld.dispatchEvent(new Event(feld instanceof HTMLSelectElement ? 'change' : 'input'));
    feld.dispatchEvent(new Event('blur'));
  }

  async function absenden(): Promise<void> {
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  async function liste(gutscheine: Gutschein[]): Promise<void> {
    http.expectOne('/api/v1/gutscheine').flush(gutscheine);
    await fixture.whenStable();
  }

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [Gutscheine],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApi(''),
        { provide: LOCALE_ID, useValue: 'de-DE' },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Gutscheine);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('AK8: zeigt Laden und danach den Leerzustand', async () => {
    expect(element.textContent).toContain('Gutscheine werden geladen');

    await liste([]);

    expect(element.textContent).toContain('Noch keine Gutscheine angelegt.');
  });

  it('AK8: zeigt einen Ladefehler mit Erneut versuchen', async () => {
    http
      .expectOne('/api/v1/gutscheine')
      .flush(null, { status: 500, statusText: 'Internal Server Error' });
    await fixture.whenStable();

    element.querySelector<HTMLButtonElement>('[role="alert"] button')!.click();
    await liste([HERBST10]);
    expect(element.textContent).toContain('HERBST10');
  });

  it('AK6: listet Code, Rabatt, Ablaufdatum und Gültigkeit', async () => {
    await liste([HERBST10, MINUS5]);

    const zeilen = element.querySelectorAll('tbody tr');
    expect(zeilen[0].textContent).toContain('HERBST10');
    expect(zeilen[0].textContent).toContain('12,5 %');
    expect(zeilen[0].textContent).toContain('31.12.2026');
    expect(zeilen[0].textContent).toContain('gültig');
    expect(zeilen[1].textContent).toMatch(/5,00\s€/);
    expect(zeilen[1].textContent).toContain('abgelaufen');
  });

  it('AK1: legt einen Prozentgutschein an und lädt die Liste neu', async () => {
    await liste([]);
    eingeben('code', 'herbst10');
    eingeben('rabattwert', '12,5');
    eingeben('ablaufdatum', '2026-12-31');

    await absenden();

    const anfrage = http.expectOne((r) => r.method === 'POST' && r.url === '/api/v1/gutscheine');
    expect(anfrage.request.body).toEqual({
      code: 'herbst10',
      rabattArt: 'PROZENT',
      prozent: 12.5,
      ablaufdatum: '2026-12-31',
    });
    anfrage.flush(HERBST10, { status: 201, statusText: 'Created' });
    await fixture.whenStable();
    await liste([HERBST10]);

    expect(element.textContent).toContain('Gutschein HERBST10 angelegt.');
    expect(element.querySelectorAll('tbody tr').length).toBe(1);
  });

  it('AK2: legt einen Festbetrag in Cent an', async () => {
    await liste([]);
    eingeben('code', 'MINUS5');
    eingeben('rabattArt', 'FESTBETRAG');
    eingeben('rabattwert', '5');
    eingeben('ablaufdatum', '2026-12-31');

    await absenden();

    const anfrage = http.expectOne((r) => r.method === 'POST');
    expect(anfrage.request.body).toEqual({
      code: 'MINUS5',
      rabattArt: 'FESTBETRAG',
      festbetrag: { cent: 500, waehrung: 'EUR' },
      ablaufdatum: '2026-12-31',
    });
    anfrage.flush(MINUS5, { status: 201, statusText: 'Created' });
    await fixture.whenStable();
    await liste([MINUS5]);
  });

  it('AK4: sendet ungültigen Code oder Rabattwert nicht', async () => {
    await liste([]);
    eingeben('code', 'HERBST 10');
    eingeben('rabattwert', '10,555');

    await absenden();

    http.expectNone((r) => r.method === 'POST');
    expect(element.textContent).toContain('Bitte 1–50 Buchstaben');
    expect(element.textContent).toContain('Bitte einen Prozentwert');
    expect(element.textContent).toContain('Bitte ein Ablaufdatum');
  });

  it('AK4/AK5: zeigt Ablehnungen des Backends, Eingaben bleiben', async () => {
    await liste([]);
    eingeben('code', 'HERBST10');
    eingeben('rabattwert', '10');
    eingeben('ablaufdatum', '2026-11-11');

    await absenden();

    http
      .expectOne((r) => r.method === 'POST')
      .flush(
        {
          title: 'Fachlich abgelehnt',
          status: 422,
          detail: 'Das Ablaufdatum 2026-11-11 liegt in der Vergangenheit.',
        },
        { status: 422, statusText: 'Unprocessable Entity' },
      );
    await fixture.whenStable();

    expect(element.querySelector('[role="alert"]')!.textContent).toContain(
      'liegt in der Vergangenheit',
    );
    expect(element.querySelector<HTMLInputElement>('#code')!.value).toBe('HERBST10');
  });
});
