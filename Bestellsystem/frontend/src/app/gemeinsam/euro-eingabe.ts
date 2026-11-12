import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

const EURO_BETRAG = /^(\d{1,12})(?:[.,](\d{1,2}))?$/;

/**
 * Wandelt eine Euro-Eingabe wie "12,99" in ganze Cent (1299) um, ohne Gleitkommarechnung (D-11).
 * Liefert null bei ungültiger Eingabe.
 */
export function euroInCent(eingabe: string | null | undefined): number | null {
  const treffer = EURO_BETRAG.exec((eingabe ?? '').trim());
  if (!treffer) {
    return null;
  }
  const euro = treffer[1];
  const cent = (treffer[2] ?? '').padEnd(2, '0');
  return Number(euro + cent);
}

export const euroBetrag: ValidatorFn = (feld: AbstractControl): ValidationErrors | null =>
  !feld.value || euroInCent(feld.value) !== null ? null : { euroBetrag: true };

/** Cent als Eingabetext im deutschen Format (4999 → "49,99"), zum Vorbelegen von Formularen. */
export function centAlsEuroEingabe(cent: number): string {
  const euro = Math.trunc(cent / 100);
  const rest = String(cent % 100).padStart(2, '0');
  return `${euro},${rest}`;
}
