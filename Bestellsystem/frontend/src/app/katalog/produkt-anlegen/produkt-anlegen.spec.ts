import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { provideApi } from '../../api';
import { ProduktAnlegen } from './produkt-anlegen';

describe('ProduktAnlegen', () => {
  let fixture: ComponentFixture<ProduktAnlegen>;
  let http: HttpTestingController;
  let element: HTMLElement;

  function eingeben(id: string, wert: string): void {
    const feld = element.querySelector<HTMLInputElement>(`#${id}`)!;
    feld.value = wert;
    feld.dispatchEvent(new Event('input'));
    feld.dispatchEvent(new Event('blur'));
  }

  async function absenden(): Promise<void> {
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [ProduktAnlegen],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApi(''),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ProduktAnlegen);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('sendet den Preis in Cent und kehrt zur Liste zurück', async () => {
    const navigiere = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    eingeben('titel', 'Stehlampe');
    eingeben('listenpreis', '49,99');
    eingeben('lagerbestand', '3');

    await absenden();

    const anfrage = http.expectOne('/api/v1/produkte');
    expect(anfrage.request.method).toBe('POST');
    expect(anfrage.request.body).toEqual({
      titel: 'Stehlampe',
      listenpreis: { cent: 4999, waehrung: 'EUR' },
      lagerbestand: 3,
    });
    anfrage.flush({}, { status: 201, statusText: 'Created' });
    await fixture.whenStable();
    expect(navigiere).toHaveBeenCalledWith(['/produkte']);
  });

  it('sendet nicht bei ungültigen Eingaben und markiert die Felder', async () => {
    eingeben('listenpreis', '12,345');
    eingeben('lagerbestand', '-1');

    await absenden();

    http.expectNone('/api/v1/produkte');
    expect(element.textContent).toContain('Bitte einen Titel');
    expect(element.textContent).toContain('Bitte einen Betrag');
    expect(element.textContent).toContain('ganze Zahl ab 0');
  });

  it('zeigt die Ablehnung des Backends verständlich an', async () => {
    eingeben('titel', 'Stehlampe');
    eingeben('listenpreis', '1');

    await absenden();
    http.expectOne('/api/v1/produkte').flush(
      {
        title: 'Ungültige Eingabe',
        detail: 'Die Eingabe ist ungültig.',
        fehler: [{ feld: 'titel', meldung: 'darf nicht leer sein' }],
      },
      { status: 400, statusText: 'Bad Request' },
    );
    await fixture.whenStable();

    const meldung = element.querySelector('[role="alert"]')!.textContent;
    expect(meldung).toContain('Ungültige Eingabe: Die Eingabe ist ungültig.');
    expect(meldung).toContain('titel: darf nicht leer sein');
    expect(element.querySelector('button')!.disabled).toBe(false);
  });
});
