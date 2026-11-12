import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Versandpaket, provideApi } from '../../api';
import { Versandpakete } from './versandpakete';

const DHL: Versandpaket = {
  id: 'v1',
  dienstleister: 'DHL',
  sendungsnummer: 'AB123DE',
  lieferstatus: 'ANGELEGT',
  angelegtAm: '2026-11-12T09:30:00Z',
  statusGeaendertAm: '2026-11-12T09:30:00Z',
  erlaubteFolgestatus: ['UEBERGEBEN', 'IN_ZUSTELLUNG', 'ZUGESTELLT'],
};

describe('Versandpakete', () => {
  let fixture: ComponentFixture<Versandpakete>;
  let http: HttpTestingController;
  let element: HTMLElement;

  const PAKETE = '/api/v1/kunden/k1/bestellungen/B-2026-000123/pakete';

  async function erzeugen(pakete: Versandpaket[]): Promise<void> {
    fixture = TestBed.createComponent(Versandpakete);
    fixture.componentRef.setInput('kundenId', 'k1');
    fixture.componentRef.setInput('bestellnummer', 'B-2026-000123');
    fixture.componentRef.setInput('pakete', pakete);
    element = fixture.nativeElement;
    await fixture.whenStable();
  }

  function waehle(selektor: string, wert: string): void {
    const auswahl = element.querySelector<HTMLSelectElement>(selektor)!;
    auswahl.value = wert;
    auswahl.dispatchEvent(new Event('change'));
  }

  function tippe(selektor: string, wert: string): void {
    const feld = element.querySelector<HTMLInputElement>(selektor)!;
    feld.value = wert;
    feld.dispatchEvent(new Event('input'));
  }

  function knopf(text: string): HTMLButtonElement {
    return [...element.querySelectorAll<HTMLButtonElement>('button')].find((b) =>
      b.textContent!.includes(text),
    )!;
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [Versandpakete],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideApi('')],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('US-07.3 AK2: zeigt ohne Paket den Hinweis', async () => {
    await erzeugen([]);

    expect(element.querySelector('.keine-pakete')!.textContent).toContain(
      'Noch kein Paket versandt.',
    );
    expect(element.querySelector('.pakete')).toBeNull();
  });

  it('US-07.3 AK1: zeigt Dienstleister, Nummer, Status und Zeitpunkte', async () => {
    await erzeugen([
      { ...DHL, lieferstatus: 'IN_ZUSTELLUNG', statusGeaendertAm: '2026-11-13T08:00:00Z' },
      { ...DHL, id: 'v2', dienstleister: 'HERMES', sendungsnummer: 'XY9' },
    ]);

    const zeilen = element.querySelectorAll('.pakete tbody tr');
    expect(zeilen.length).toBe(2);
    expect(zeilen[0].textContent).toContain('DHL');
    expect(zeilen[0].textContent).toContain('AB123DE');
    expect(zeilen[0].textContent).toContain('in Zustellung');
    expect(zeilen[0].textContent).toContain('12.11.2026');
    expect(zeilen[0].textContent).toContain('13.11.2026');
    expect(zeilen[1].textContent).toContain('Hermes');
    expect(zeilen[1].textContent).toContain('angelegt');
  });

  it('US-07.2 AK6: bietet nur die erlaubten Folgestatus vom Backend an', async () => {
    await erzeugen([
      DHL,
      { ...DHL, id: 'v2', lieferstatus: 'ZUGESTELLT', erlaubteFolgestatus: [] },
    ]);

    const optionen = [...element.querySelectorAll<HTMLOptionElement>('#status-v1 option')].map(
      (o) => o.value,
    );
    expect(optionen).toEqual(['', 'UEBERGEBEN', 'IN_ZUSTELLUNG', 'ZUGESTELLT']);
    expect(element.querySelector('#status-v2')).toBeNull();
    expect(element.querySelectorAll('.pakete tbody tr')[1].textContent).toContain('abgeschlossen');
  });

  it('US-07.2 AK1: setzt den Status und zeigt die Antwort', async () => {
    await erzeugen([DHL]);

    waehle('#status-v1', 'UEBERGEBEN');
    knopf('Status setzen').click();
    const anfrage = http.expectOne(`${PAKETE}/v1/status`);
    expect(anfrage.request.method).toBe('PUT');
    expect(anfrage.request.body).toEqual({ lieferstatus: 'UEBERGEBEN' });
    anfrage.flush({
      ...DHL,
      lieferstatus: 'UEBERGEBEN',
      statusGeaendertAm: '2026-11-13T08:00:00Z',
      erlaubteFolgestatus: ['IN_ZUSTELLUNG', 'ZUGESTELLT', 'RETOURE', 'VERLOREN'],
    } satisfies Versandpaket);
    await fixture.whenStable();

    const zeile = element.querySelector('.pakete tbody tr')!;
    expect(zeile.textContent).toContain('übergeben');
    expect(zeile.textContent).toContain('13.11.2026');
  });

  it('setzt ohne gewählten Status nichts', async () => {
    await erzeugen([DHL]);

    knopf('Status setzen').click();

    http.expectNone(`${PAKETE}/v1/status`);
  });

  it('US-07.2 AK4: zeigt eine fachliche Ablehnung verständlich an', async () => {
    await erzeugen([DHL]);

    waehle('#status-v1', 'ZUGESTELLT');
    knopf('Status setzen').click();
    http.expectOne(`${PAKETE}/v1/status`).flush(
      {
        title: 'Fachlich abgelehnt',
        detail: 'Der Lieferstatus eines Pakets kann nicht von ZUGESTELLT nach ZUGESTELLT wechseln.',
      },
      { status: 422, statusText: 'Unprocessable Entity' },
    );
    await fixture.whenStable();

    expect(element.querySelector('[role="alert"]')!.textContent).toContain(
      'kann nicht von ZUGESTELLT nach ZUGESTELLT wechseln',
    );
    expect(element.querySelector('.pakete tbody tr')!.textContent).toContain('angelegt');
  });

  it('US-07.1 AK1/AK3: legt ein Paket an und hängt es an die Liste', async () => {
    await erzeugen([DHL]);

    waehle('#dienstleister', 'DPD');
    tippe('#sendungsnummer', ' xy9 ');
    knopf('Paket anlegen').click();
    const anfrage = http.expectOne(PAKETE);
    expect(anfrage.request.method).toBe('POST');
    expect(anfrage.request.body).toEqual({ dienstleister: 'DPD', sendungsnummer: ' xy9 ' });
    anfrage.flush({ ...DHL, id: 'v2', dienstleister: 'DPD', sendungsnummer: 'XY9' });
    await fixture.whenStable();

    const zeilen = element.querySelectorAll('.pakete tbody tr');
    expect(zeilen.length).toBe(2);
    expect(zeilen[1].textContent).toContain('XY9');
    expect(element.querySelector<HTMLInputElement>('#sendungsnummer')!.value).toBe('');
  });

  it('US-07.1 AK6: verlangt Dienstleister und Sendungsnummer', async () => {
    await erzeugen([]);

    knopf('Paket anlegen').click();
    await fixture.whenStable();

    http.expectNone(PAKETE);
    expect(element.textContent).toContain('Bitte einen Dienstleister wählen.');
    expect(element.textContent).toContain('Bitte eine Sendungsnummer eingeben.');
  });

  it('US-07.1 AK5: zeigt eine vergebene Sendungsnummer als Konflikt', async () => {
    await erzeugen([]);

    waehle('#dienstleister', 'DHL');
    tippe('#sendungsnummer', 'AB123DE');
    knopf('Paket anlegen').click();
    http
      .expectOne(PAKETE)
      .flush(
        { title: 'Konflikt', detail: 'Sendungsnummer AB123DE ist bei DHL schon vergeben.' },
        { status: 409, statusText: 'Conflict' },
      );
    await fixture.whenStable();

    expect(element.querySelector('[role="alert"]')!.textContent).toContain(
      'Sendungsnummer AB123DE ist bei DHL schon vergeben.',
    );
    expect(element.querySelector('.keine-pakete')).not.toBeNull();
  });
});
