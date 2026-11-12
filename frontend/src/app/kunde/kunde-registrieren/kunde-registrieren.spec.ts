import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { provideApi } from '../../api';
import { KundeRegistrieren } from './kunde-registrieren';

describe('KundeRegistrieren', () => {
  let fixture: ComponentFixture<KundeRegistrieren>;
  let http: HttpTestingController;
  let element: HTMLElement;

  function eingeben(id: string, wert: string): void {
    const feld = element.querySelector<HTMLInputElement>(`#${id}`)!;
    feld.value = wert;
    feld.dispatchEvent(new Event('input'));
    feld.dispatchEvent(new Event('blur'));
  }

  function vollstaendigAusfuellen(): void {
    eingeben('vorname', 'Anna');
    eingeben('nachname', 'Muster');
    eingeben('email', 'anna@example.de');
    eingeben('strasse', 'Hauptstraße 1');
    eingeben('postleitzahl', '10115');
    eingeben('stadt', 'Berlin');
  }

  async function absenden(): Promise<void> {
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [KundeRegistrieren],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApi(''),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(KundeRegistrieren);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('US-01.1 AK1, AK5: sendet Stammdaten und Lieferadresse und öffnet das Adressbuch', async () => {
    const navigiere = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    vollstaendigAusfuellen();
    element.querySelector<HTMLInputElement>('#istAuchRechnungsadresse')!.click();

    await absenden();

    const anfrage = http.expectOne('/api/v1/kunden');
    expect(anfrage.request.method).toBe('POST');
    expect(anfrage.request.body).toEqual({
      vorname: 'Anna',
      nachname: 'Muster',
      email: 'anna@example.de',
      lieferadresse: {
        strasse: 'Hauptstraße 1',
        stadt: 'Berlin',
        postleitzahl: '10115',
        land: 'DE',
      },
      istAuchRechnungsadresse: true,
    });
    anfrage.flush({ id: 'k1' }, { status: 201, statusText: 'Created' });
    await fixture.whenStable();
    expect(navigiere).toHaveBeenCalledWith(['/kunden', 'k1']);
  });

  it('US-01.1 AK4: sendet nicht ohne vollständige Adresse und benennt die Felder', async () => {
    eingeben('vorname', 'Anna');
    eingeben('nachname', 'Muster');
    eingeben('email', 'anna@example.de');
    eingeben('land', 'DEU');

    await absenden();

    http.expectNone('/api/v1/kunden');
    expect(element.textContent).toContain('Bitte eine Straße');
    expect(element.textContent).toContain('Bitte eine Postleitzahl');
    expect(element.textContent).toContain('Bitte eine Stadt');
    expect(element.textContent).toContain('zweistelligen Ländercode');
  });

  it('US-01.1 AK3: zeigt die vergebene E-Mail verständlich an', async () => {
    vollstaendigAusfuellen();

    await absenden();
    http.expectOne('/api/v1/kunden').flush(
      {
        title: 'Konflikt',
        detail: 'Die E-Mail-Adresse anna@example.de ist bereits vergeben.',
      },
      { status: 409, statusText: 'Conflict' },
    );
    await fixture.whenStable();

    expect(element.querySelector('[role="alert"]')!.textContent).toContain(
      'Konflikt: Die E-Mail-Adresse anna@example.de ist bereits vergeben.',
    );
    expect(element.querySelector<HTMLButtonElement>('button[type="submit"]')!.disabled).toBe(false);
  });
});
