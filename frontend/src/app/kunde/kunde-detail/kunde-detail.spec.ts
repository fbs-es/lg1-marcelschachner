import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Adresse, Kunde, provideApi } from '../../api';
import { KundeDetail } from './kunde-detail';

const BERLIN: Adresse = {
  id: 'a1',
  anschrift: { strasse: 'Hauptstraße 1', stadt: 'Berlin', postleitzahl: '10115', land: 'DE' },
  istLieferadresse: true,
  istRechnungsadresse: false,
};
const WIEN: Adresse = {
  id: 'a2',
  anschrift: { strasse: 'Ring 2', stadt: 'Wien', postleitzahl: '1010', land: 'AT' },
  istLieferadresse: false,
  istRechnungsadresse: true,
};
const ANNA: Kunde = {
  id: 'k1',
  vorname: 'Anna',
  nachname: 'Muster',
  email: 'anna@example.de',
  adressen: [BERLIN, WIEN],
};

describe('KundeDetail', () => {
  let fixture: ComponentFixture<KundeDetail>;
  let http: HttpTestingController;
  let element: HTMLElement;

  const karten = () => element.querySelectorAll<HTMLElement>('.adressen > li');

  function knopf(bereich: HTMLElement, text: string): HTMLButtonElement {
    return [...bereich.querySelectorAll<HTMLButtonElement>('button')].find((b) =>
      b.textContent!.includes(text),
    )!;
  }

  function eingeben(id: string, wert: string): void {
    const feld = element.querySelector<HTMLInputElement>(`#${id}`)!;
    feld.value = wert;
    feld.dispatchEvent(new Event('input'));
    feld.dispatchEvent(new Event('blur'));
  }

  function checkbox(beschriftung: string): HTMLInputElement {
    return [...element.querySelectorAll('label.auswahl')]
      .find((l) => l.textContent!.includes(beschriftung))!
      .querySelector('input')!;
  }

  async function absenden(): Promise<void> {
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  async function geladen(kunde: Kunde = ANNA): Promise<void> {
    http.expectOne('/api/v1/kunden/k1').flush(kunde);
    await fixture.whenStable();
    http.expectOne('/api/v1/kunden/k1/bestellungen').flush([]);
    await fixture.whenStable();
  }

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [KundeDetail],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApi(''),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(KundeDetail);
    fixture.componentRef.setInput('kundenId', 'k1');
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('zeigt Laden, dann Stammdaten und Adressbuch mit Kennzeichen', async () => {
    expect(element.textContent).toContain('Kunde wird geladen');
    await geladen();

    expect(element.querySelector('h1')!.textContent).toContain('Anna Muster');
    expect(element.textContent).toContain('anna@example.de');
    expect(karten().length).toBe(2);
    expect(karten()[0].textContent).toContain('10115 Berlin');
    expect(karten()[0].textContent).toContain('Lieferadresse');
    expect(karten()[0].textContent).not.toContain('Rechnungsadresse');
    expect(karten()[1].textContent).toContain('Rechnungsadresse');
  });

  it('zeigt einen unbekannten Kunden als Fehler', async () => {
    http
      .expectOne('/api/v1/kunden/k1')
      .flush(
        { title: 'Nicht gefunden', detail: 'Kunde k1 existiert nicht' },
        { status: 404, statusText: 'Not Found' },
      );
    await fixture.whenStable();
    expect(element.querySelector('[role="alert"]')!.textContent).toContain(
      'Kunde k1 existiert nicht',
    );
  });

  it('US-01.2 AK1: fügt eine Adresse hinzu und zeigt sie im Adressbuch', async () => {
    await geladen({ ...ANNA, adressen: [BERLIN] });
    knopf(element, 'Adresse hinzufügen').click();
    await fixture.whenStable();
    eingeben('adresse-strasse', 'Ring 2');
    eingeben('adresse-postleitzahl', '1010');
    eingeben('adresse-stadt', 'Wien');
    eingeben('adresse-land', 'AT');
    checkbox('Lieferadresse').click();
    checkbox('Rechnungsadresse').click();

    await absenden();

    const anfrage = http.expectOne('/api/v1/kunden/k1/adressen');
    expect(anfrage.request.method).toBe('POST');
    expect(anfrage.request.body).toEqual({
      anschrift: WIEN.anschrift,
      istLieferadresse: false,
      istRechnungsadresse: true,
    });
    anfrage.flush(WIEN, { status: 201, statusText: 'Created' });
    await fixture.whenStable();
    expect(karten().length).toBe(2);
    expect(karten()[1].textContent).toContain('1010 Wien');
    expect(element.querySelector('form')).toBeNull();
  });

  it('US-01.2 AK2: sendet keine Adresse ohne Kennzeichen', async () => {
    await geladen();
    knopf(element, 'Adresse hinzufügen').click();
    await fixture.whenStable();
    eingeben('adresse-strasse', 'Ring 2');
    eingeben('adresse-postleitzahl', '1010');
    eingeben('adresse-stadt', 'Wien');
    checkbox('Lieferadresse').click();

    await absenden();

    http.expectNone('/api/v1/kunden/k1/adressen');
    expect(element.textContent).toContain('Bitte Liefer- und/oder Rechnungsadresse wählen');
  });

  it('US-01.3 AK1: ändert eine Adresse mit vorbelegtem Formular', async () => {
    await geladen();
    knopf(karten()[1], 'Bearbeiten').click();
    await fixture.whenStable();
    expect(element.querySelector<HTMLInputElement>('#adresse-stadt')!.value).toBe('Wien');
    eingeben('adresse-stadt', 'Graz');

    await absenden();

    const anfrage = http.expectOne('/api/v1/kunden/k1/adressen/a2');
    expect(anfrage.request.method).toBe('PUT');
    expect(anfrage.request.body.anschrift.stadt).toBe('Graz');
    expect(anfrage.request.body.istRechnungsadresse).toBe(true);
    anfrage.flush({ ...WIEN, anschrift: { ...WIEN.anschrift, stadt: 'Graz' } });
    await fixture.whenStable();
    expect(karten()[1].textContent).toContain('1010 Graz');
  });

  it('US-01.3 AK2: zeigt die Ablehnung beim Entziehen des Lieferkennzeichens im Formular', async () => {
    await geladen();
    knopf(karten()[0], 'Bearbeiten').click();
    await fixture.whenStable();
    checkbox('Lieferadresse').click();
    checkbox('Rechnungsadresse').click();

    await absenden();
    http.expectOne('/api/v1/kunden/k1/adressen/a1').flush(
      {
        title: 'Fachlich abgelehnt',
        detail: 'Die letzte Lieferadresse muss Lieferadresse bleiben (I-1).',
      },
      { status: 422, statusText: 'Unprocessable Entity' },
    );
    await fixture.whenStable();

    expect(element.querySelector('form [role="alert"]')!.textContent).toContain(
      'Die letzte Lieferadresse muss Lieferadresse bleiben',
    );
  });

  it('US-01.3 AK4: löscht eine Adresse aus dem Adressbuch', async () => {
    await geladen();
    knopf(karten()[1], 'Löschen').click();

    const anfrage = http.expectOne('/api/v1/kunden/k1/adressen/a2');
    expect(anfrage.request.method).toBe('DELETE');
    anfrage.flush(null, { status: 204, statusText: 'No Content' });
    await fixture.whenStable();
    expect(karten().length).toBe(1);
  });

  it('US-01.3 AK5: zeigt die Ablehnung beim Löschen der letzten Lieferadresse', async () => {
    await geladen();
    knopf(karten()[0], 'Löschen').click();

    http.expectOne('/api/v1/kunden/k1/adressen/a1').flush(
      {
        title: 'Fachlich abgelehnt',
        detail: 'Die letzte Lieferadresse kann nicht gelöscht werden (I-1).',
      },
      { status: 422, statusText: 'Unprocessable Entity' },
    );
    await fixture.whenStable();

    expect(element.querySelector('[role="alert"]')!.textContent).toContain(
      'Fachlich abgelehnt: Die letzte Lieferadresse kann nicht gelöscht werden (I-1).',
    );
    expect(karten().length).toBe(2);
  });
});
