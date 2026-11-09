import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BeitragRequest } from '../../models/api.models';
import { BeitragFormular } from './beitrag-formular';

describe('BeitragFormular', () => {
  let fixture: ComponentFixture<BeitragFormular>;
  let komponente: BeitragFormular;

  beforeEach(async () => {
    fixture = TestBed.createComponent(BeitragFormular);
    komponente = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('zeigt die Felder passend zum Typ', async () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('input[name="kuenstler"]')).not.toBeNull();

    const auswahl = element.querySelector('select[name="typ"]') as HTMLSelectElement;
    auswahl.value = 'WERBUNG';
    auswahl.dispatchEvent(new Event('change'));
    await fixture.whenStable();

    expect(element.querySelector('input[name="kuenstler"]')).toBeNull();
    expect(element.querySelector('input[name="kunde"]')).not.toBeNull();
  });

  it('erstellt einen Musikbeitrag mit Typ', () => {
    let gesendet: BeitragRequest | undefined;
    komponente.absenden.subscribe((r) => (gesendet = r));
    komponente.titel = 'Wolkenlos';
    komponente.dauer = 240;
    komponente.kuenstler = 'Die Wolken';
    komponente.album = 'Himmelblau';

    komponente.senden();

    expect(gesendet).toEqual({
      typ: 'MUSIK',
      titel: 'Wolkenlos',
      dauer: 240,
      kuenstler: 'Die Wolken',
      album: 'Himmelblau',
      genre: 'POP',
    });
  });

  it('erstellt eine Eilmeldung nur mit ihren Feldern', () => {
    komponente.typ = 'EILMELDUNG';
    komponente.titel = 'Sturmwarnung';
    komponente.dauer = 45;
    komponente.region = 'Tirol';

    expect(komponente.erstelleRequest()).toEqual({
      typ: 'EILMELDUNG',
      titel: 'Sturmwarnung',
      dauer: 45,
      dringlichkeitsstufe: 3,
      region: 'Tirol',
    });
  });
});
