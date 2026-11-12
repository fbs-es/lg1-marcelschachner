import { BestellungAufgeben, Zahlung } from '../api';

type Zahlungsart = BestellungAufgeben.ZahlungsartEnum;

/** Anzeigenamen in der Reihenfolge der Auswahl im Checkout. */
export const ZAHLUNGSARTEN: readonly { wert: Zahlungsart; name: string }[] = [
  { wert: BestellungAufgeben.ZahlungsartEnum.Rechnung, name: 'Rechnung' },
  { wert: BestellungAufgeben.ZahlungsartEnum.Lastschrift, name: 'Lastschrift' },
  { wert: BestellungAufgeben.ZahlungsartEnum.Kreditkarte, name: 'Kreditkarte' },
  { wert: BestellungAufgeben.ZahlungsartEnum.Paypal, name: 'PayPal' },
  { wert: BestellungAufgeben.ZahlungsartEnum.Vorkasse, name: 'Vorkasse' },
];

const TRANSAKTIONSSTATUS: Record<Zahlung.TransaktionsstatusEnum, string> = {
  AUSSTEHEND: 'ausstehend',
  ERFOLGREICH: 'erfolgreich',
  FEHLGESCHLAGEN: 'fehlgeschlagen',
  STORNIERT: 'storniert',
};

export function zahlungsartName(art: Zahlungsart): string {
  return ZAHLUNGSARTEN.find((z) => z.wert === art)?.name ?? art;
}

export function transaktionsstatusName(status: Zahlung.TransaktionsstatusEnum): string {
  return TRANSAKTIONSSTATUS[status] ?? status;
}
