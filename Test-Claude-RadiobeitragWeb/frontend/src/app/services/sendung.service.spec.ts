import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EilmeldungErgebnisDto, SendeplanDto } from '../models/api.models';
import { SendungService } from './sendung.service';

describe('SendungService', () => {
  let service: SendungService;
  let http: HttpTestingController;
  const plan = { id: 3, beitraege: [] } as unknown as SendeplanDto;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SendungService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('liest den Sendeplan', () => {
    let ergebnis: SendeplanDto | undefined;
    service.sendeplanLesen(3).subscribe((p) => (ergebnis = p));

    const anfrage = http.expectOne('/api/sendungen/3');
    expect(anfrage.request.method).toBe('GET');
    anfrage.flush(plan);
    expect(ergebnis).toEqual(plan);
  });

  it('sendet neue Beiträge mit Typ-Diskriminator', () => {
    service
      .beitragHinzufuegen(3, { typ: 'MODERATION', titel: 'Begrüßung', dauer: 60, sprechertext: 'Guten Morgen!' })
      .subscribe();

    const anfrage = http.expectOne('/api/sendungen/3/beitraege');
    expect(anfrage.request.method).toBe('POST');
    expect(anfrage.request.body.typ).toBe('MODERATION');
    anfrage.flush(plan);
  });

  it('kürzt, verschiebt und löscht Beiträge', () => {
    service.beitragKuerzen(3, 10, 30).subscribe();
    service.beitragVerschieben(3, 10, 0).subscribe();
    service.beitragLoeschen(3, 10).subscribe();

    const kuerzen = http.expectOne('/api/sendungen/3/beitraege/10/kuerzen');
    const verschieben = http.expectOne('/api/sendungen/3/beitraege/10/verschieben');
    const loeschen = http.expectOne('/api/sendungen/3/beitraege/10');
    expect(kuerzen.request.body).toEqual({ neueDauer: 30 });
    expect(verschieben.request.body).toEqual({ neuerIndex: 0 });
    expect(loeschen.request.method).toBe('DELETE');
    kuerzen.flush(plan);
    verschieben.flush(plan);
    loeschen.flush(plan);
  });

  it('fügt eine Eilmeldung mit Laufzeit ein', () => {
    let ergebnis: EilmeldungErgebnisDto | undefined;
    service
      .eilmeldungEinfuegen(3, { titel: 'Sturmwarnung', dauer: 45, dringlichkeitsstufe: 4, region: 'Tirol', laufzeit: 160 })
      .subscribe((e) => (ergebnis = e));

    const anfrage = http.expectOne('/api/sendungen/3/eilmeldung');
    expect(anfrage.request.body.laufzeit).toBe(160);
    anfrage.flush({ sendeplan: plan, warnung: 'WARNUNG an Marcel Schachner' });
    expect(ergebnis?.warnung).toBe('WARNUNG an Marcel Schachner');
  });
});
