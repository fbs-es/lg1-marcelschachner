import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Produkt, provideApi } from '../../api';
import { ProduktListe } from './produkt-liste';

const LAMPE: Produkt = {
  id: '1',
  titel: 'Stehlampe',
  listenpreis: { cent: 4999, waehrung: 'EUR' },
  lagerbestand: 3,
  istVerfuegbar: true,
};

describe('ProduktListe', () => {
  let fixture: ComponentFixture<ProduktListe>;
  let http: HttpTestingController;

  const text = () => (fixture.nativeElement as HTMLElement).textContent ?? '';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ProduktListe],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApi(''),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ProduktListe);
  });

  afterEach(() => http.verify());

  it('zeigt den Ladezustand', async () => {
    await fixture.whenStable();
    expect(text()).toContain('Produkte werden geladen');
    http.expectOne('/api/v1/produkte');
  });

  it('US-02.1: zeigt Produkte mit Preis und Verfügbarkeit vom Backend', async () => {
    http
      .expectOne('/api/v1/produkte')
      .flush([
        LAMPE,
        { ...LAMPE, id: '2', titel: 'Tischlampe', lagerbestand: 0, istVerfuegbar: false },
      ]);
    await fixture.whenStable();

    const zeilen = (fixture.nativeElement as HTMLElement).querySelectorAll('tbody tr');
    expect(zeilen.length).toBe(2);
    expect(zeilen[0].textContent).toContain('Stehlampe');
    expect(zeilen[0].textContent).toContain('49,99 €');
    expect(zeilen[0].textContent).toContain('verfügbar');
    expect(zeilen[1].textContent).toContain('nicht verfügbar');
  });

  it('zeigt den Leerzustand', async () => {
    http.expectOne('/api/v1/produkte').flush([]);
    await fixture.whenStable();
    expect(text()).toContain('Noch keine Produkte im Katalog');
  });

  it('zeigt Fehler verständlich und lädt erneut', async () => {
    http
      .expectOne('/api/v1/produkte')
      .flush(
        { title: 'Interner Fehler', detail: 'Ein unerwarteter Fehler ist aufgetreten.' },
        { status: 500, statusText: 'Server Error' },
      );
    await fixture.whenStable();
    expect(text()).toContain('Interner Fehler: Ein unerwarteter Fehler ist aufgetreten.');

    (fixture.nativeElement as HTMLElement).querySelector('button')!.click();
    http.expectOne('/api/v1/produkte').flush([LAMPE]);
    await fixture.whenStable();
    expect(text()).toContain('Stehlampe');
  });
});
