import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { provideApi } from '../../api';
import { KundenListe } from './kunden-liste';

describe('KundenListe', () => {
  let fixture: ComponentFixture<KundenListe>;
  let http: HttpTestingController;

  const element = () => fixture.nativeElement as HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [KundenListe],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApi(''),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(KundenListe);
  });

  afterEach(() => http.verify());

  it('zeigt den Ladezustand', async () => {
    await fixture.whenStable();
    expect(element().textContent).toContain('Kunden werden geladen');
    http.expectOne('/api/v1/kunden');
  });

  it('zeigt Kunden mit Link auf das Adressbuch', async () => {
    http
      .expectOne('/api/v1/kunden')
      .flush([{ id: 'k1', vorname: 'Anna', nachname: 'Albers', email: 'anna@example.de' }]);
    await fixture.whenStable();

    const link = element().querySelector('tbody a')!;
    expect(link.textContent).toContain('Albers, Anna');
    expect(link.getAttribute('href')).toBe('/kunden/k1');
    expect(element().textContent).toContain('anna@example.de');
  });

  it('zeigt den Leerzustand', async () => {
    http.expectOne('/api/v1/kunden').flush([]);
    await fixture.whenStable();
    expect(element().textContent).toContain('Noch keine Kunden registriert');
  });

  it('zeigt Fehler und lädt auf Wunsch erneut', async () => {
    http
      .expectOne('/api/v1/kunden')
      .flush({ title: 'Interner Fehler' }, { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();
    expect(element().querySelector('[role="alert"]')!.textContent).toContain('Interner Fehler');

    element().querySelector<HTMLButtonElement>('[role="alert"] button')!.click();
    http.expectOne('/api/v1/kunden').flush([]);
    await fixture.whenStable();
    expect(element().textContent).toContain('Noch keine Kunden');
  });
});
