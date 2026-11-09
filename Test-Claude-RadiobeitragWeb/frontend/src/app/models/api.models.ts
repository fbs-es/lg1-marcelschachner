export type BeitragTyp = 'MUSIK' | 'WERBUNG' | 'MODERATION' | 'EILMELDUNG';

export type Musikgenre =
  | 'POP'
  | 'ROCK'
  | 'JAZZ'
  | 'KLASSIK'
  | 'HIPHOP'
  | 'ELEKTRONISCH'
  | 'SCHLAGER';

export const MUSIKGENRES: Musikgenre[] = [
  'POP',
  'ROCK',
  'JAZZ',
  'KLASSIK',
  'HIPHOP',
  'ELEKTRONISCH',
  'SCHLAGER',
];

export const BEITRAG_TYPEN: { wert: BeitragTyp; text: string }[] = [
  { wert: 'MUSIK', text: 'Musik' },
  { wert: 'WERBUNG', text: 'Werbung' },
  { wert: 'MODERATION', text: 'Moderation' },
  { wert: 'EILMELDUNG', text: 'Eilmeldung' },
];

export interface SenderRequest {
  name: string;
  frequenz: number;
  datum: string;
}

export interface SenderDto {
  id: number;
  name: string;
  frequenz: number;
  programmId: number;
  datum: string;
}

export interface SendungRequest {
  name: string;
  moderator: string;
  startzeit: string;
  endzeit: string;
  maximaldauer: number;
}

export interface SendungDto {
  id: number;
  name: string;
  moderator: string;
  startzeit: string;
  endzeit: string;
  maximaldauer: number;
  gesamtdauer: number;
  restzeit: number;
  zeitlimitUeberschritten: boolean;
}

export interface ProgrammDto {
  id: number;
  datum: string;
  sendungen: SendungDto[];
}

export interface BeitragDto {
  id: number;
  typ: BeitragTyp;
  titel: string;
  dauer: number;
  startzeitpunkt: number;
  kuenstler: string | null;
  album: string | null;
  genre: Musikgenre | null;
  kunde: string | null;
  werbebudget: number | null;
  sprechertext: string | null;
  dringlichkeitsstufe: number | null;
  region: string | null;
}

export interface SendeplanDto extends SendungDto {
  beitraege: BeitragDto[];
}

export interface MusikbeitragRequest {
  typ: 'MUSIK';
  titel: string;
  dauer: number;
  kuenstler: string;
  album: string;
  genre: Musikgenre;
}

export interface WerbebeitragRequest {
  typ: 'WERBUNG';
  titel: string;
  dauer: number;
  kunde: string;
  werbebudget: number;
}

export interface ModerationRequest {
  typ: 'MODERATION';
  titel: string;
  dauer: number;
  sprechertext: string;
}

export interface EilmeldungRequest {
  typ: 'EILMELDUNG';
  titel: string;
  dauer: number;
  dringlichkeitsstufe: number;
  region: string;
}

export type BeitragRequest =
  | MusikbeitragRequest
  | WerbebeitragRequest
  | ModerationRequest
  | EilmeldungRequest;

export interface EilmeldungEinfuegenRequest {
  titel: string;
  dauer: number;
  dringlichkeitsstufe: number;
  region: string;
  laufzeit: number;
}

export interface EilmeldungErgebnisDto {
  sendeplan: SendeplanDto;
  warnung: string | null;
}

export interface FehlerDto {
  status: number;
  message: string;
}
