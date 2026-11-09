import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ProgrammService } from '../../services/programm.service';
import { SenderService } from '../../services/sender.service';
import { ProgrammUebersicht } from './programm-uebersicht';

describe('ProgrammUebersicht', () => {
  let fixture: ComponentFixture<ProgrammUebersicht>;
  let element: HTMLElement;

  beforeEach(async () => {
    const senderService = {
      senderAuflisten: vi
        .fn()
        .mockReturnValue(of([{ id: 1, name: 'Radio AirControl', frequenz: 98.5, programmId: 2, datum: '2026-11-09' }])),
    };
    const programmService = {
      programmLesen: vi.fn().mockReturnValue(
        of({
          id: 2,
          datum: '2026-11-09',
          sendungen: [
            {
              id: 5,
              name: 'Morgenshow',
              moderator: 'Marcel Schachner',
              startzeit: '08:00:00',
              endzeit: '09:00:00',
              maximaldauer: 600,
              gesamtdauer: 645,
              restzeit: -45,
              zeitlimitUeberschritten: true,
            },
          ],
        }),
      ),
    };
    TestBed.configureTestingModule({
      imports: [ProgrammUebersicht],
      providers: [
        provideRouter([]),
        { provide: SenderService, useValue: senderService },
        { provide: ProgrammService, useValue: programmService },
      ],
    });
    fixture = TestBed.createComponent(ProgrammUebersicht);
    element = fixture.nativeElement as HTMLElement;
    await fixture.whenStable();
  });

  it('zeigt die Sendungen des Tages in Zeitleiste und Liste', () => {
    const block = element.querySelector('.zeitleiste .block') as HTMLElement;
    const zeile = element.querySelector('tbody tr') as HTMLElement;

    expect(element.textContent).toContain('09.11.2026');
    expect(block.textContent).toContain('Morgenshow');
    expect(block.classList).toContain('ueber');
    expect(block.style.left).toBe(`${(8 / 24) * 100}%`);
    expect(zeile.textContent).toContain('08:00–09:00');
    expect(zeile.textContent).toContain('Marcel Schachner');
    expect(zeile.textContent).toContain('-0:45');
  });
});
