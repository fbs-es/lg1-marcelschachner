import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EMPTY, catchError, map, of, switchMap, tap } from 'rxjs';

import {
  Adresse,
  BestellungAufgeben,
  BestellungenService,
  KundeUebersicht,
  KundenService,
} from '../../api';
import { GeldPipe } from '../../gemeinsam/geld.pipe';
import { problemMeldung } from '../../gemeinsam/problem-meldung';
import { ZAHLUNGSARTEN } from '../bezeichnungen';
import { Warenkorb, Warenkorbeintrag } from '../warenkorb';

type Zustand = 'laedt' | 'fehler' | 'geladen';

/** Warenkorb und Kasse (US-04.1, US-08.1): Kunde, Lieferadresse, Zahlungsart, Gutschein. */
@Component({
  selector: 'app-checkout',
  imports: [ReactiveFormsModule, RouterLink, GeldPipe],
  templateUrl: './checkout.html',
  styleUrl: './checkout.css',
})
export class Checkout {
  private readonly kundenService = inject(KundenService);
  private readonly bestellungenService = inject(BestellungenService);
  private readonly router = inject(Router);
  private readonly formBuilder = inject(NonNullableFormBuilder);

  protected readonly warenkorb = inject(Warenkorb);
  protected readonly zahlungsarten = ZAHLUNGSARTEN;

  protected readonly formular = this.formBuilder.group({
    kundenId: ['', Validators.required],
    lieferadresseId: ['', Validators.required],
    zahlungsart: [
      BestellungAufgeben.ZahlungsartEnum.Rechnung as BestellungAufgeben.ZahlungsartEnum,
      Validators.required,
    ],
    gutscheincode: ['', Validators.maxLength(50)],
  });

  protected readonly kundenZustand = signal<Zustand>('laedt');
  protected readonly kunden = signal<KundeUebersicht[]>([]);
  protected readonly kundenFehler = signal('');

  /** `null`, solange kein Kunde gewählt ist. */
  protected readonly adressenZustand = signal<Zustand | null>(null);
  protected readonly lieferadressen = signal<Adresse[]>([]);
  protected readonly adressenFehler = signal('');

  protected readonly sendet = signal(false);
  protected readonly fehlermeldung = signal('');

  constructor() {
    this.ladeKunden();
    // switchMap verwirft die Antwort eines zuvor gewählten Kunden.
    this.formular.controls.kundenId.valueChanges
      .pipe(
        tap(() => {
          this.formular.controls.lieferadresseId.setValue('');
          this.lieferadressen.set([]);
        }),
        switchMap((kundenId) => {
          if (!kundenId) {
            this.adressenZustand.set(null);
            return EMPTY;
          }
          this.adressenZustand.set('laedt');
          return this.kundenService.findeKunde(kundenId).pipe(
            map((kunde) => ({ adressen: kunde.adressen.filter((a) => a.istLieferadresse) })),
            catchError((fehler) => of({ fehler: problemMeldung(fehler) })),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe((ergebnis) => {
        if ('fehler' in ergebnis) {
          this.adressenFehler.set(ergebnis.fehler);
          this.adressenZustand.set('fehler');
          return;
        }
        this.lieferadressen.set(ergebnis.adressen);
        if (ergebnis.adressen.length > 0) {
          this.formular.controls.lieferadresseId.setValue(ergebnis.adressen[0].id);
        }
        this.adressenZustand.set('geladen');
      });
  }

  protected ladeKunden(): void {
    this.kundenZustand.set('laedt');
    this.kundenService.listeKunden().subscribe({
      next: (kunden) => {
        this.kunden.set(kunden);
        this.kundenZustand.set('geladen');
      },
      error: (fehler) => {
        this.kundenFehler.set(problemMeldung(fehler));
        this.kundenZustand.set('fehler');
      },
    });
  }

  protected ladeLieferadressenErneut(): void {
    const kundenId = this.formular.controls.kundenId;
    kundenId.setValue(kundenId.value);
  }

  protected aendereMenge(eintrag: Warenkorbeintrag, feld: HTMLInputElement): void {
    const menge = feld.valueAsNumber;
    if (!Number.isInteger(menge) || menge < 1) {
      feld.value = String(this.warenkorb.mengeVon(eintrag.produkt.id));
      return;
    }
    this.warenkorb.aendereMenge(eintrag.produkt.id, menge);
  }

  protected bestellen(): void {
    if (this.warenkorb.istLeer() || this.sendet()) {
      return;
    }
    if (this.formular.invalid) {
      this.formular.markAllAsTouched();
      return;
    }
    const { kundenId, lieferadresseId, zahlungsart, gutscheincode } = this.formular.getRawValue();
    this.sendet.set(true);
    this.fehlermeldung.set('');
    this.bestellungenService
      .gibBestellungAuf(kundenId, {
        positionen: this.warenkorb
          .eintraege()
          .map((e) => ({ produktId: e.produkt.id, menge: e.menge })),
        lieferadresseId,
        zahlungsart,
        gutscheincode: gutscheincode.trim() || undefined,
      })
      .subscribe({
        next: (bestellung) => {
          this.warenkorb.leere();
          this.router.navigate(['/kunden', kundenId, 'bestellungen', bestellung.bestellnummer], {
            queryParams: { aufgegeben: 'ja' },
          });
        },
        error: (fehler) => {
          this.fehlermeldung.set(problemMeldung(fehler));
          this.sendet.set(false);
        },
      });
  }

  protected istUngueltig(feld: 'kundenId' | 'lieferadresseId' | 'gutscheincode'): boolean {
    const control = this.formular.controls[feld];
    return control.invalid && control.touched;
  }
}
