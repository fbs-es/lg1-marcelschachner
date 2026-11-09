import { formatDatum, formatDauer, formatUhrzeit, formatUhrzeitKurz, sekundenAusUhrzeit } from './zeit';

describe('zeit', () => {
  it('formatiert Dauern als m:ss', () => {
    expect(formatDauer(0)).toBe('0:00');
    expect(formatDauer(245)).toBe('4:05');
    expect(formatDauer(-45)).toBe('-0:45');
  });

  it('rechnet Uhrzeiten in Sekunden um', () => {
    expect(sekundenAusUhrzeit('08:00:00')).toBe(28800);
    expect(sekundenAusUhrzeit('08:30')).toBe(30600);
  });

  it('formatiert Startzeit plus Versatz als Uhrzeit', () => {
    expect(formatUhrzeit('08:00:00', 160)).toBe('08:02:40');
    expect(formatUhrzeit('23:59:00', 90)).toBe('00:00:30');
    expect(formatUhrzeitKurz('08:00:00')).toBe('08:00');
  });

  it('formatiert ein Datum deutsch', () => {
    expect(formatDatum('2026-11-09')).toBe('09.11.2026');
  });
});
