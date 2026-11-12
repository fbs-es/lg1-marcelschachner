import { Versandpaket } from '../api';

type Lieferstatus = Versandpaket.LieferstatusEnum;
type Dienstleister = Versandpaket.DienstleisterEnum;

const LIEFERSTATUS: Record<Lieferstatus, string> = {
  ANGELEGT: 'angelegt',
  UEBERGEBEN: 'übergeben',
  IN_ZUSTELLUNG: 'in Zustellung',
  ZUGESTELLT: 'zugestellt',
  RETOURE: 'Retoure',
  VERLOREN: 'verloren',
};

/** Anzeigenamen in der Reihenfolge der Auswahl beim Anlegen. */
export const DIENSTLEISTER: readonly { wert: Dienstleister; name: string }[] = [
  { wert: Versandpaket.DienstleisterEnum.Dhl, name: 'DHL' },
  { wert: Versandpaket.DienstleisterEnum.Dpd, name: 'DPD' },
  { wert: Versandpaket.DienstleisterEnum.Gls, name: 'GLS' },
  { wert: Versandpaket.DienstleisterEnum.Hermes, name: 'Hermes' },
  { wert: Versandpaket.DienstleisterEnum.Ups, name: 'UPS' },
];

export function lieferstatusName(status: Lieferstatus): string {
  return LIEFERSTATUS[status] ?? status;
}

export function dienstleisterName(dienstleister: Dienstleister): string {
  return DIENSTLEISTER.find((d) => d.wert === dienstleister)?.name ?? dienstleister;
}
