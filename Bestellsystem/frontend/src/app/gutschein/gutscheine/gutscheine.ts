import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';

import { Gutschein, GutscheinAnlegen, GutscheineService } from '../../api';
import { euroInCent } from '../../gemeinsam/euro-eingabe';
import { GeldPipe } from '../../gemeinsam/geld.pipe';
import { problemMeldung } from '../../gemeinsam/problem-meldung';

type Zustand = 'laedt' | 'fehler' | 'geladen';
type RabattArt = GutscheinAnlegen.RabattArtEnum;

const RABATTART = GutscheinAnlegen.RabattArtEnum;
const PROZENT_EINGABE = /^\d{1,3}(?:[.,]\d{1,2})?$/;

/** Prüft nur das Eingabeformat; Grenzen wie „höchstens 100 %“ prüft das Backend (D-23). */
function rabattwertFormat(gruppe: AbstractControl): ValidationErrors | null {
  const rabattArt = gruppe.get('rabattArt')!.value as RabattArt;
  const wert = String(gruppe.get('rabattwert')!.value ?? '').trim();
  if (!wert) {
    return null;
  }
  const gueltig =
    rabattArt === RABATTART.Prozent ? PROZENT_EINGABE.test(wert) : euroInCent(wert) !== null;
  return gueltig ? null : { rabattwertFormat: true };
}

/** US-08.2: Gutscheine ansehen und anlegen (D-23). */
@Component({
  selector: 'app-gutscheine',
  imports: [ReactiveFormsModule, GeldPipe, DatePipe, DecimalPipe],
  templateUrl: './gutscheine.html',
})
export class Gutscheine {
  private readonly gutscheineService = inject(GutscheineService);

  protected readonly rabattarten = RABATTART;
  protected readonly zustand = signal<Zustand>('laedt');
  protected readonly gutscheine = signal<Gutschein[]>([]);
  protected readonly fehlermeldung = signal('');
  protected readonly sendet = signal(false);
  protected readonly aktionsfehler = signal('');
  protected readonly erfolg = signal('');

  protected readonly formular = inject(NonNullableFormBuilder).group(
    {
      code: ['', [Validators.required, Validators.pattern(/^\s*[A-Za-z0-9-]{1,50}\s*$/)]],
      rabattArt: [RABATTART.Prozent as RabattArt, Validators.required],
      rabattwert: ['', Validators.required],
      ablaufdatum: ['', Validators.required],
    },
    { validators: rabattwertFormat },
  );

  constructor() {
    this.laden();
  }

  protected laden(): void {
    this.zustand.set('laedt');
    this.gutscheineService.listeGutscheine().subscribe({
      next: (gutscheine) => {
        this.gutscheine.set(gutscheine);
        this.zustand.set('geladen');
      },
      error: (fehler) => {
        this.fehlermeldung.set(problemMeldung(fehler));
        this.zustand.set('fehler');
      },
    });
  }

  protected anlegen(): void {
    if (this.formular.invalid) {
      this.formular.markAllAsTouched();
      return;
    }
    const werte = this.formular.getRawValue();
    const wert = werte.rabattwert.trim();
    const eingabe: GutscheinAnlegen = {
      code: werte.code,
      rabattArt: werte.rabattArt,
      ablaufdatum: werte.ablaufdatum,
      ...(werte.rabattArt === RABATTART.Prozent
        ? { prozent: Number(wert.replace(',', '.')) }
        : { festbetrag: { cent: euroInCent(wert)!, waehrung: 'EUR' } }),
    };
    this.sendet.set(true);
    this.aktionsfehler.set('');
    this.erfolg.set('');
    this.gutscheineService.legeGutscheinAn(eingabe).subscribe({
      next: (gutschein) => {
        this.erfolg.set(`Gutschein ${gutschein.code} angelegt.`);
        this.formular.reset();
        this.sendet.set(false);
        this.laden();
      },
      error: (fehler) => {
        this.aktionsfehler.set(problemMeldung(fehler));
        this.sendet.set(false);
      },
    });
  }

  protected istUngueltig(feld: 'code' | 'rabattwert' | 'ablaufdatum'): boolean {
    const control = this.formular.controls[feld];
    const formatFehler = feld === 'rabattwert' && this.formular.hasError('rabattwertFormat');
    return (control.invalid || formatFehler) && control.touched;
  }
}
