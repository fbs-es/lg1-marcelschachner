import { Injectable, computed, signal } from '@angular/core';

import { Produkt } from '../api';

export interface Warenkorbeintrag {
  produkt: Produkt;
  menge: number;
}

/**
 * Warenkorb nur im Browser-Speicher (E04: keine persistente Entität). Bestand, Preise und
 * Rabatt prüft und berechnet erst das Backend beim Bestellen.
 */
@Injectable({ providedIn: 'root' })
export class Warenkorb {
  private readonly inhalt = signal<Warenkorbeintrag[]>([]);

  readonly eintraege = this.inhalt.asReadonly();
  readonly stueckzahl = computed(() => this.inhalt().reduce((summe, e) => summe + e.menge, 0));
  readonly istLeer = computed(() => this.inhalt().length === 0);

  lege(produkt: Produkt, menge = 1): void {
    this.inhalt.update((eintraege) =>
      eintraege.some((e) => e.produkt.id === produkt.id)
        ? eintraege.map((e) => (e.produkt.id === produkt.id ? { ...e, menge: e.menge + menge } : e))
        : [...eintraege, { produkt, menge }],
    );
  }

  aendereMenge(produktId: string, menge: number): void {
    if (!Number.isInteger(menge) || menge < 1) {
      return;
    }
    this.inhalt.update((eintraege) =>
      eintraege.map((e) => (e.produkt.id === produktId ? { ...e, menge } : e)),
    );
  }

  entferne(produktId: string): void {
    this.inhalt.update((eintraege) => eintraege.filter((e) => e.produkt.id !== produktId));
  }

  mengeVon(produktId: string): number {
    return this.inhalt().find((e) => e.produkt.id === produktId)?.menge ?? 0;
  }

  leere(): void {
    this.inhalt.set([]);
  }
}
