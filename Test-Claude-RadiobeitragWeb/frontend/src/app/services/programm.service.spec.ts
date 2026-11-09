import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ProgrammService } from './programm.service';
import { SenderService } from './sender.service';

describe('ProgrammService und SenderService', () => {
  let programmService: ProgrammService;
  let senderService: SenderService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    programmService = TestBed.inject(ProgrammService);
    senderService = TestBed.inject(SenderService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('liest Programm und Sendungen', () => {
    programmService.programmLesen(1).subscribe();
    programmService.sendungenAuflisten(1).subscribe();

    http.expectOne('/api/programme/1').flush({ id: 1, datum: '2026-11-09', sendungen: [] });
    http.expectOne('/api/programme/1/sendungen').flush([]);
  });

  it('legt eine Sendung an', () => {
    const request = {
      name: 'Morgenshow',
      moderator: 'Marcel Schachner',
      startzeit: '08:00',
      endzeit: '09:00',
      maximaldauer: 900,
    };
    programmService.sendungAnlegen(1, request).subscribe();

    const anfrage = http.expectOne('/api/programme/1/sendungen');
    expect(anfrage.request.method).toBe('POST');
    expect(anfrage.request.body).toEqual(request);
    anfrage.flush({});
  });

  it('legt Sender an und listet sie auf', () => {
    senderService.senderAnlegen({ name: 'Radio AirControl', frequenz: 98.5, datum: '2026-11-09' }).subscribe();
    senderService.senderAuflisten().subscribe();
    senderService.senderLesen(1).subscribe();

    expect(http.expectOne({ method: 'POST', url: '/api/sender' }).request.body.frequenz).toBe(98.5);
    http.expectOne({ method: 'GET', url: '/api/sender' }).flush([]);
    http.expectOne('/api/sender/1').flush({});
  });
});
