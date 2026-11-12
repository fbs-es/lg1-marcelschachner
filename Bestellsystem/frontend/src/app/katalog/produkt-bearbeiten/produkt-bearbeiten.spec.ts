import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';

import { Produkt, provideApi } from '../../api';
import { ProduktBearbeiten } from './produkt-bearbeiten';

registerLocaleData(localeDe);

const LAMPE: Produkt = {
  id: 'p1',
  titel: 'Stehlampe',
  listenpreis: { cent: 4999, waehrung: 'EUR' },
  lagerbestand: 3,
  istVerfuegbar: true,
};

describe('ProduktBearbeiten (US-02.2)', () => {
  let fixture: ComponentFixture<ProduktBearbeiten>;
  let http: HttpTestingController;
  let element: HTMLElement;

  function feld(id: string): HTMLInputElement {
    return element.querySelector<HTMLInputElement>(`#${id}`)!;
  }

  function eingeben(id: string, wert: string): void {
    feld(id).value = wert;
    feld(id).dispatchEvent(new Event('input'));
    feld(id).dispatchEvent(new Event('blur'));
  }

  async function absenden(index: number): Promise<void> {
    element.querySelectorAll('form')[index].dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  async function geladen(): Promise<void> {
    http.expectOne('/api/v1/produkte/p1').flush(LAMPE);
    await fixture.whenStable();
  }

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [ProduktBearbeiten],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApi(''),
        { provide: LOCALE_ID, useValue: 'de-DE' },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ProduktBearbeiten);
    fixture.componentRef.setInput('produktId', 'p1');
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('zeigt beim Laden einen Hinweis und belegt danach die Formulare vor', async () => {
    expect(element.textContent).toContain('Produkt wird geladen');

    await geladen();

    expect(element.querySelector('h1')!.textContent).toContain('Stehlampe');
    expect(feld('titel').value).toBe('Stehlampe');
    expect(feld('listenpreis').value).toBe('49,99');
    expect(feld('lagerbestand').value).toBe('3');
  });

  it('zeigt einen Ladefehler mit Erneut versuchen', async () => {
    http
      .expectOne('/api/v1/produkte/p1')
      .flush(
        { title: 'Nicht gefunden', status: 404, detail: 'Produkt p1 existiert nicht' },
        { status: 404, statusText: 'Not Found' },
      );
    await fixture.whenStable();

    expect(element.querySelector('[role="alert"]')!.textContent).toContain(
      'Produkt p1 existiert nicht',
    );
    element.querySelector<HTMLButtonElement>('[role="alert"] button')!.click();
    await geladen();
    expect(feld('titel').value).toBe('Stehlampe');
  });

  it('AK1: speichert Titel und Preis in Cent und bestätigt', async () => {
    await geladen();
    eingeben('titel', 'Stehlampe Nova');
    eingeben('listenpreis', '59,99');

    await absenden(0);

    const anfrage = http.expectOne('/api/v1/produkte/p1');
    expect(anfrage.request.method).toBe('PUT');
    expect(anfrage.request.body).toEqual({
      titel: 'Stehlampe Nova',
      listenpreis: { cent: 5999, waehrung: 'EUR' },
    });
    anfrage.flush({
      ...LAMPE,
      titel: 'Stehlampe Nova',
      listenpreis: { cent: 5999, waehrung: 'EUR' },
    });
    await fixture.whenStable();

    expect(element.textContent).toContain('Stammdaten gespeichert.');
    expect(element.querySelector('h1')!.textContent).toContain('Stehlampe Nova');
  });

  it('AK2: sendet bei ungültigen Stammdaten nicht und markiert die Felder', async () => {
    await geladen();
    eingeben('titel', '');
    eingeben('listenpreis', '12,345');

    await absenden(0);

    http.expectNone('/api/v1/produkte/p1');
    expect(element.textContent).toContain('Bitte einen Titel');
    expect(element.textContent).toContain('Bitte einen Betrag');
  });

  it('AK6: zeigt einen Versionskonflikt als Meldung, Eingaben bleiben', async () => {
    await geladen();
    eingeben('titel', 'Stehlampe Nova');

    await absenden(0);

    http
      .expectOne('/api/v1/produkte/p1')
      .flush(
        { title: 'Versionskonflikt', status: 409, detail: 'Gleichzeitig geändert' },
        { status: 409, statusText: 'Conflict' },
      );
    await fixture.whenStable();

    expect(element.querySelector('[role="alert"]')!.textContent).toContain('Gleichzeitig geändert');
    expect(feld('titel').value).toBe('Stehlampe Nova');
  });

  it('setzt den Lagerbestand über den eigenen Endpunkt', async () => {
    await geladen();
    eingeben('lagerbestand', '7');

    await absenden(1);

    const anfrage = http.expectOne('/api/v1/produkte/p1/lagerbestand');
    expect(anfrage.request.method).toBe('PUT');
    expect(anfrage.request.body).toEqual({ lagerbestand: 7 });
    anfrage.flush({ ...LAMPE, lagerbestand: 7 });
    await fixture.whenStable();

    expect(element.textContent).toContain('Lagerbestand gespeichert.');
    expect(element.textContent).toContain('Bestand 7');
  });

  it('sendet keinen negativen Lagerbestand', async () => {
    await geladen();
    eingeben('lagerbestand', '-1');

    await absenden(1);

    http.expectNone('/api/v1/produkte/p1/lagerbestand');
    expect(element.textContent).toContain('ganze Zahl ab 0');
  });
});
