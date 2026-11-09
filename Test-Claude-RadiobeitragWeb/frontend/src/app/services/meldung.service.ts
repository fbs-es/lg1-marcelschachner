import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';

export type MeldungArt = 'erfolg' | 'fehler' | 'warnung';

export interface Meldung {
  id: number;
  art: MeldungArt;
  text: string;
}

export function fehlertext(fehler: unknown): string {
  if (fehler instanceof HttpErrorResponse) {
    if (fehler.status === 0) {
      return 'Backend nicht erreichbar!';
    }
    const body = fehler.error as { message?: unknown } | null;
    if (body && typeof body.message === 'string') {
      return body.message;
    }
    return `Fehler ${fehler.status}: ${fehler.statusText}`;
  }
  return 'Unbekannter Fehler!';
}

@Injectable({ providedIn: 'root' })
export class MeldungService {
  private naechsteId = 1;
  readonly meldungen = signal<Meldung[]>([]);

  erfolg(text: string): void {
    this.zeigen('erfolg', text, 4000);
  }

  warnung(text: string): void {
    this.zeigen('warnung', text, 0);
  }

  fehler(fehler: unknown): void {
    this.zeigen('fehler', fehlertext(fehler), 8000);
  }

  schliessen(id: number): void {
    this.meldungen.update((liste) => liste.filter((m) => m.id !== id));
  }

  private zeigen(art: MeldungArt, text: string, dauerMs: number): void {
    const meldung: Meldung = { id: this.naechsteId++, art, text };
    this.meldungen.update((liste) => [...liste, meldung]);
    if (dauerMs > 0) {
      setTimeout(() => this.schliessen(meldung.id), dauerMs);
    }
  }
}
