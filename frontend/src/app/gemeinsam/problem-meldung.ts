import { HttpErrorResponse } from '@angular/common/http';

interface Feldfehler {
  feld: string;
  meldung: string;
}

/** Macht aus einer Fehlerantwort (ProblemDetail, RFC 9457) einen verständlichen Text. */
export function problemMeldung(fehler: unknown): string {
  if (!(fehler instanceof HttpErrorResponse)) {
    return 'Ein unerwarteter Fehler ist aufgetreten.';
  }
  if (fehler.status === 0) {
    return 'Der Server ist nicht erreichbar. Bitte später erneut versuchen.';
  }
  const problem = fehler.error as { title?: string; detail?: string; fehler?: Feldfehler[] } | null;
  if (!problem || typeof problem !== 'object' || (!problem.title && !problem.detail)) {
    return `Die Anfrage ist fehlgeschlagen (Status ${fehler.status}).`;
  }
  const kopf = [problem.title, problem.detail].filter(Boolean).join(': ');
  const felder = (problem.fehler ?? []).map((f) => `${f.feld}: ${f.meldung}`);
  return [kopf, ...felder].join('\n');
}
