import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { BeitragDto, SendeplanDto } from '../../models/api.models';
import { MeldungService } from '../../services/meldung.service';
import { SendungService } from '../../services/sendung.service';
import { Sendeplan } from './sendeplan';

function beitrag(id: number, titel: string, startzeitpunkt: number, dauer: number): BeitragDto {
  return {
    id,
    typ: 'MODERATION',
    titel,
    dauer,
    startzeitpunkt,
    kuenstler: null,
    album: null,
    genre: null,
    kunde: null,
    werbebudget: null,
    sprechertext: 'Guten Morgen!',
    dringlichkeitsstufe: null,
    region: null,
  };
}

function plan(gesamtdauer: number): SendeplanDto {
  return {
    id: 3,
    name: 'Morgenshow',
    moderator: 'Marcel Schachner',
    startzeit: '08:00:00',
    endzeit: '09:00:00',
    maximaldauer: 600,
    gesamtdauer,
    restzeit: 600 - gesamtdauer,
    zeitlimitUeberschritten: gesamtdauer > 600,
    beitraege: [beitrag(10, 'Begrüßung', 0, 60), beitrag(11, 'Verabschiedung', 60, 30)],
  };
}

describe('Sendeplan', () => {
  let fixture: ComponentFixture<Sendeplan>;
  let element: HTMLElement;
  let sendungService: {
    sendeplanLesen: ReturnType<typeof vi.fn>;
    beitragVerschieben: ReturnType<typeof vi.fn>;
    beitragLoeschen: ReturnType<typeof vi.fn>;
    beitragHinzufuegen: ReturnType<typeof vi.fn>;
    eilmeldungEinfuegen: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    sendungService = {
      sendeplanLesen: vi.fn().mockReturnValue(of(plan(90))),
      beitragVerschieben: vi.fn().mockReturnValue(of(plan(90))),
      beitragLoeschen: vi.fn().mockReturnValue(of(plan(90))),
      beitragHinzufuegen: vi.fn(),
      eilmeldungEinfuegen: vi.fn().mockReturnValue(
        of({ sendeplan: plan(645), warnung: 'WARNUNG an Marcel Schachner: Zeitlimit um 45 Sekunden überschritten!' }),
      ),
    };
    TestBed.configureTestingModule({
      imports: [Sendeplan],
      providers: [provideRouter([]), { provide: SendungService, useValue: sendungService }],
    });
    fixture = TestBed.createComponent(Sendeplan);
    fixture.componentRef.setInput('id', '3');
    element = fixture.nativeElement as HTMLElement;
    await fixture.whenStable();
  });

  it('zeigt den Sendeplan mit Startzeiten', () => {
    const zeilen = element.querySelectorAll('tbody tr');

    expect(sendungService.sendeplanLesen).toHaveBeenCalledWith(3);
    expect(zeilen).toHaveLength(2);
    expect(zeilen[1].textContent).toContain('08:01:00');
    expect(zeilen[1].textContent).toContain('Verabschiedung');
    expect(element.querySelector('.banner')).toBeNull();
  });

  it('verschiebt einen Beitrag nach oben', () => {
    const knopf = element.querySelectorAll('tbody tr')[1].querySelector('[aria-label="Nach oben"]') as HTMLButtonElement;

    knopf.click();

    expect(sendungService.beitragVerschieben).toHaveBeenCalledWith(3, 11, 0);
  });

  it('löscht einen Beitrag', () => {
    const knopf = element.querySelector('tbody tr .loeschen') as HTMLButtonElement;

    knopf.click();

    expect(sendungService.beitragLoeschen).toHaveBeenCalledWith(3, 10);
  });

  it('zeigt die Warnung nach einer Eilmeldung deutlich an', async () => {
    const meldungen = TestBed.inject(MeldungService);

    fixture.componentInstance.eilmeldungEinfuegen({
      titel: 'Sturmwarnung',
      dauer: 45,
      dringlichkeitsstufe: 4,
      region: 'Tirol',
      laufzeit: 30,
    });
    await fixture.whenStable();

    const banner = element.querySelector('.banner');
    expect(banner?.textContent).toContain('Zeitlimit überschritten um 0:45');
    expect(banner?.textContent).toContain('WARNUNG an Marcel Schachner');
    expect(meldungen.meldungen()[0].art).toBe('warnung');
  });

  it('zeigt Fehler aus dem Backend an', () => {
    const meldungen = TestBed.inject(MeldungService);
    sendungService.beitragHinzufuegen.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: { status: 409, message: 'Beitrag überschreitet die Maximaldauer der Sendung!' },
          }),
      ),
    );

    fixture.componentInstance.beitragHinzufuegen({
      typ: 'MODERATION',
      titel: 'Zu lang',
      dauer: 900,
      sprechertext: 'Guten Morgen!',
    });

    expect(meldungen.meldungen()[0].text).toBe('Beitrag überschreitet die Maximaldauer der Sendung!');
  });
});
