import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Produkt, ProdukteService } from '../../api';
import { Warenkorb } from '../../bestellung/warenkorb';
import { GeldPipe } from '../../gemeinsam/geld.pipe';
import { problemMeldung } from '../../gemeinsam/problem-meldung';

type Zustand = 'laedt' | 'fehler' | 'geladen';

@Component({
  selector: 'app-produkt-liste',
  imports: [RouterLink, GeldPipe],
  templateUrl: './produkt-liste.html',
  styleUrl: './produkt-liste.css',
})
export class ProduktListe {
  private readonly produkteService = inject(ProdukteService);
  protected readonly warenkorb = inject(Warenkorb);

  protected readonly zustand = signal<Zustand>('laedt');
  protected readonly produkte = signal<Produkt[]>([]);
  protected readonly fehlermeldung = signal('');

  constructor() {
    this.laden();
  }

  protected laden(): void {
    this.zustand.set('laedt');
    this.produkteService.listeProdukte().subscribe({
      next: (produkte) => {
        this.produkte.set(produkte);
        this.zustand.set('geladen');
      },
      error: (fehler) => {
        this.fehlermeldung.set(problemMeldung(fehler));
        this.zustand.set('fehler');
      },
    });
  }
}
