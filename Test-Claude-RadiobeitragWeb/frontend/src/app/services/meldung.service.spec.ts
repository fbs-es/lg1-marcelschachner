import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MeldungService, fehlertext } from './meldung.service';

describe('MeldungService', () => {
  it('liest die deutsche Meldung aus dem Backend', () => {
    const fehler = new HttpErrorResponse({
      status: 409,
      error: { status: 409, message: 'Beitrag überschreitet die Maximaldauer der Sendung!' },
    });

    expect(fehlertext(fehler)).toBe('Beitrag überschreitet die Maximaldauer der Sendung!');
    expect(fehlertext(new HttpErrorResponse({ status: 0 }))).toBe('Backend nicht erreichbar!');
  });

  it('zeigt Warnungen dauerhaft bis zum Schließen', () => {
    const service = TestBed.inject(MeldungService);

    service.warnung('WARNUNG an Marcel Schachner');
    const meldung = service.meldungen()[0];
    expect(meldung.art).toBe('warnung');

    service.schliessen(meldung.id);
    expect(service.meldungen()).toHaveLength(0);
  });
});
