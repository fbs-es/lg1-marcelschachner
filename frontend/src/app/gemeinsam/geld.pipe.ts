import { formatCurrency, getCurrencySymbol, registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';
import { Pipe, PipeTransform } from '@angular/core';

import { Geld } from '../api';

registerLocaleData(localeDe);

/** Zeigt einen Betrag aus Cent im deutschen Format an, z. B. 4999 → "49,99 €". */
@Pipe({ name: 'geld' })
export class GeldPipe implements PipeTransform {
  transform(geld: Geld | null | undefined): string {
    if (!geld) {
      return '';
    }
    return formatCurrency(
      geld.cent / 100,
      'de-DE',
      getCurrencySymbol(geld.waehrung, 'narrow', 'de-DE'),
      geld.waehrung,
    );
  }
}
