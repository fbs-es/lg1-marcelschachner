import { FormControl } from '@angular/forms';
import { euroBetrag, euroInCent } from './euro-eingabe';

describe('euroInCent', () => {
  it.each([
    ['12,99', 1299],
    ['12.99', 1299],
    ['12,9', 1290],
    ['12', 1200],
    [' 0,10 ', 10],
    ['0', 0],
    ['1234567,89', 123456789],
  ])('%s → %i Cent', (eingabe, cent) => {
    expect(euroInCent(eingabe)).toBe(cent);
  });

  it.each(['', 'abc', '-1', '1,999', '1.234,56', '1e3', ',5'])('lehnt "%s" ab', (eingabe) => {
    expect(euroInCent(eingabe)).toBeNull();
  });

  it('rechnet ohne Gleitkommafehler', () => {
    // 0.29 * 100 ergäbe 28.999999999999996
    expect(euroInCent('0,29')).toBe(29);
  });
});

describe('euroBetrag-Validator', () => {
  it('meldet ungültige Beträge', () => {
    expect(new FormControl('12,5x', euroBetrag).errors).toEqual({ euroBetrag: true });
    expect(new FormControl('12,50', euroBetrag).errors).toBeNull();
  });
});
