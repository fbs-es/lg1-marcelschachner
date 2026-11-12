import { Component, OnInit, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Produkt, ProdukteService } from '../../api';
import { centAlsEuroEingabe, euroBetrag, euroInCent } from '../../gemeinsam/euro-eingabe';
import { GeldPipe } from '../../gemeinsam/geld.pipe';
import { problemMeldung } from '../../gemeinsam/problem-meldung';

type Zustand = 'laedt' | 'fehler' | 'geladen';

/** Rückmeldung eines Formulars nach dem Absenden. */
interface Rueckmeldung {
  art: 'erfolg' | 'fehler';
  text: string;
}

/** US-02.2: Titel, Listenpreis und Lagerbestand eines Produkts ändern (D-23). */
@Component({
  selector: 'app-produkt-bearbeiten',
  imports: [ReactiveFormsModule, RouterLink, GeldPipe],
  templateUrl: './produkt-bearbeiten.html',
})
export class ProduktBearbeiten implements OnInit {
  private readonly produkteService = inject(ProdukteService);
  private readonly formularBauer = inject(NonNullableFormBuilder);

  readonly produktId = input.required<string>();

  protected readonly zustand = signal<Zustand>('laedt');
  protected readonly produkt = signal<Produkt | undefined>(undefined);
  protected readonly fehlermeldung = signal('');

  protected readonly stammdaten = this.formularBauer.group({
    titel: ['', [Validators.required, Validators.maxLength(200)]],
    listenpreis: ['', [Validators.required, euroBetrag]],
  });
  protected readonly bestand = this.formularBauer.group({
    lagerbestand: [0, [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]],
  });

  protected readonly sendetStammdaten = signal(false);
  protected readonly sendetBestand = signal(false);
  protected readonly stammdatenRueckmeldung = signal<Rueckmeldung | null>(null);
  protected readonly bestandRueckmeldung = signal<Rueckmeldung | null>(null);

  ngOnInit(): void {
    this.laden();
  }

  protected laden(): void {
    this.zustand.set('laedt');
    this.produkteService.findeProdukt(this.produktId()).subscribe({
      next: (produkt) => {
        this.uebernimm(produkt);
        this.zustand.set('geladen');
      },
      error: (fehler) => {
        this.fehlermeldung.set(problemMeldung(fehler));
        this.zustand.set('fehler');
      },
    });
  }

  protected speichereStammdaten(): void {
    if (this.stammdaten.invalid) {
      this.stammdaten.markAllAsTouched();
      return;
    }
    const werte = this.stammdaten.getRawValue();
    this.sendetStammdaten.set(true);
    this.stammdatenRueckmeldung.set(null);
    this.produkteService
      .aendereProdukt(this.produktId(), {
        titel: werte.titel,
        listenpreis: { cent: euroInCent(werte.listenpreis)!, waehrung: 'EUR' },
      })
      .subscribe({
        next: (produkt) => {
          this.uebernimm(produkt);
          this.stammdatenRueckmeldung.set({ art: 'erfolg', text: 'Stammdaten gespeichert.' });
          this.sendetStammdaten.set(false);
        },
        error: (fehler) => {
          this.stammdatenRueckmeldung.set({ art: 'fehler', text: problemMeldung(fehler) });
          this.sendetStammdaten.set(false);
        },
      });
  }

  protected speichereBestand(): void {
    if (this.bestand.invalid) {
      this.bestand.markAllAsTouched();
      return;
    }
    this.sendetBestand.set(true);
    this.bestandRueckmeldung.set(null);
    this.produkteService
      .setzeLagerbestand(this.produktId(), {
        lagerbestand: Number(this.bestand.getRawValue().lagerbestand),
      })
      .subscribe({
        next: (produkt) => {
          this.uebernimm(produkt);
          this.bestandRueckmeldung.set({ art: 'erfolg', text: 'Lagerbestand gespeichert.' });
          this.sendetBestand.set(false);
        },
        error: (fehler) => {
          this.bestandRueckmeldung.set({ art: 'fehler', text: problemMeldung(fehler) });
          this.sendetBestand.set(false);
        },
      });
  }

  protected istUngueltig(feld: 'titel' | 'listenpreis' | 'lagerbestand'): boolean {
    const control =
      feld === 'lagerbestand' ? this.bestand.controls.lagerbestand : this.stammdaten.controls[feld];
    return control.invalid && control.touched;
  }

  private uebernimm(produkt: Produkt): void {
    this.produkt.set(produkt);
    this.stammdaten.setValue({
      titel: produkt.titel,
      listenpreis: centAlsEuroEingabe(produkt.listenpreis.cent),
    });
    this.bestand.setValue({ lagerbestand: produkt.lagerbestand });
  }
}
