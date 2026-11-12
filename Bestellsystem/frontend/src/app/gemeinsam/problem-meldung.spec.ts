import { HttpErrorResponse } from '@angular/common/http';
import { problemMeldung } from './problem-meldung';

describe('problemMeldung', () => {
  it('nennt Titel, Detail und Feldfehler', () => {
    const fehler = new HttpErrorResponse({
      status: 400,
      error: {
        title: 'Ungültige Eingabe',
        detail: 'Die Eingabe ist ungültig.',
        fehler: [{ feld: 'titel', meldung: 'darf nicht leer sein' }],
      },
    });

    expect(problemMeldung(fehler)).toBe(
      'Ungültige Eingabe: Die Eingabe ist ungültig.\ntitel: darf nicht leer sein',
    );
  });

  it('meldet einen nicht erreichbaren Server', () => {
    expect(problemMeldung(new HttpErrorResponse({ status: 0 }))).toContain('nicht erreichbar');
  });

  it('fällt ohne ProblemDetail auf den Status zurück', () => {
    expect(problemMeldung(new HttpErrorResponse({ status: 502, error: 'Bad Gateway' }))).toBe(
      'Die Anfrage ist fehlgeschlagen (Status 502).',
    );
  });
});
