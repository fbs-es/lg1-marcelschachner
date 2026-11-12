import { GeldPipe } from './geld.pipe';

describe('GeldPipe', () => {
  const pipe = new GeldPipe();

  it('zeigt Cent als Euro im deutschen Format', () => {
    expect(pipe.transform({ cent: 4999, waehrung: 'EUR' })).toBe('49,99 €');
    expect(pipe.transform({ cent: 123456, waehrung: 'EUR' })).toBe('1.234,56 €');
    expect(pipe.transform({ cent: 0, waehrung: 'EUR' })).toBe('0,00 €');
  });

  it('zeigt nichts ohne Betrag', () => {
    expect(pipe.transform(undefined)).toBe('');
  });
});
