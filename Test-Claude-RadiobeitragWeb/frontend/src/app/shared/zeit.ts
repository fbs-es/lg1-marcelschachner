function zweistellig(zahl: number): string {
  return zahl.toString().padStart(2, '0');
}

export function sekundenAusUhrzeit(uhrzeit: string): number {
  const teile = uhrzeit.split(':').map((teil) => Number(teil));
  const stunden = teile[0] ?? 0;
  const minuten = teile[1] ?? 0;
  const sekunden = teile[2] ?? 0;
  return stunden * 3600 + minuten * 60 + sekunden;
}

export function formatDauer(sekunden: number): string {
  const vorzeichen = sekunden < 0 ? '-' : '';
  const betrag = Math.abs(sekunden);
  const minuten = Math.floor(betrag / 60);
  return `${vorzeichen}${minuten}:${zweistellig(betrag % 60)}`;
}

export function formatUhrzeit(startzeit: string, versatzSekunden = 0): string {
  const gesamt = (sekundenAusUhrzeit(startzeit) + versatzSekunden) % 86400;
  const stunden = Math.floor(gesamt / 3600);
  const minuten = Math.floor((gesamt % 3600) / 60);
  return `${zweistellig(stunden)}:${zweistellig(minuten)}:${zweistellig(gesamt % 60)}`;
}

export function formatUhrzeitKurz(uhrzeit: string): string {
  return uhrzeit.substring(0, 5);
}

export function formatDatum(datum: string): string {
  const [jahr, monat, tag] = datum.split('-');
  return `${tag}.${monat}.${jahr}`;
}
