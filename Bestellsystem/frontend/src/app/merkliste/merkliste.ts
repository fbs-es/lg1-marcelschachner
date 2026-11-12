import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, forkJoin } from 'rxjs';

import { MerklisteService, Merklisteneintrag, Produkt, ProdukteService } from '../api';
import { GeldPipe } from '../gemeinsam/geld.pipe';
import { problemMeldung } from '../gemeinsam/problem-meldung';

type Zustand = 'laedt' | 'fehler' | 'geladen';

/** Merkliste eines Kunden: ansehen, merken, entfernen (US-03.1, US-03.2, D-21). */
@Component({
  selector: 'app-merkliste',
  imports: [ReactiveFormsModule, RouterLink, DatePipe, GeldPipe],
  templateUrl: './merkliste.html',
  styleUrl: './merkliste.css',
})
export class Merkliste implements OnInit {
  private readonly merklisteService = inject(MerklisteService);
  private readonly produkteService = inject(ProdukteService);

  readonly kundenId = input.required<string>();

  protected readonly zustand = signal<Zustand>('laedt');
  protected readonly eintraege = signal<Merklisteneintrag[]>([]);
  protected readonly produkte = signal<Produkt[]>([]);
  protected readonly fehlermeldung = signal('');
  protected readonly aktionsfehler = signal('');
  protected readonly sendet = signal(false);

  protected readonly formular = new FormGroup({
    produktId: new FormControl('', { nonNullable: true, validators: Validators.required }),
  });
  protected readonly produktId = this.formular.controls.produktId;

  ngOnInit(): void {
    this.laden();
  }

  protected laden(): void {
    this.zustand.set('laedt');
    forkJoin({
      eintraege: this.merklisteService.listeMerkliste(this.kundenId()),
      produkte: this.produkteService.listeProdukte(),
    }).subscribe({
      next: ({ eintraege, produkte }) => {
        this.eintraege.set(eintraege);
        this.produkte.set(produkte);
        this.zustand.set('geladen');
      },
      error: (fehler) => {
        this.fehlermeldung.set(problemMeldung(fehler));
        this.zustand.set('fehler');
      },
    });
  }

  protected merken(): void {
    if (this.produktId.invalid) {
      this.produktId.markAsTouched();
      return;
    }
    this.fuehreAus(this.merklisteService.merkeProdukt(this.kundenId(), this.produktId.value), () =>
      this.formular.reset(),
    );
  }

  protected entfernen(eintrag: Merklisteneintrag): void {
    this.fuehreAus(this.merklisteService.entferneVonMerkliste(this.kundenId(), eintrag.produktId));
  }

  private fuehreAus(anfrage: Observable<unknown>, danach: () => void = () => {}): void {
    this.aktionsfehler.set('');
    this.sendet.set(true);
    anfrage.subscribe({
      next: () => {
        this.sendet.set(false);
        danach();
        this.ladeEintraege();
      },
      error: (fehler) => {
        this.sendet.set(false);
        this.aktionsfehler.set(problemMeldung(fehler));
      },
    });
  }

  private ladeEintraege(): void {
    this.merklisteService.listeMerkliste(this.kundenId()).subscribe({
      next: (eintraege) => this.eintraege.set(eintraege),
      error: (fehler) => this.aktionsfehler.set(problemMeldung(fehler)),
    });
  }
}
