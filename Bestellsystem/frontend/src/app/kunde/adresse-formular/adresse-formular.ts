import { Component, OnInit, inject, input, output } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
} from '@angular/forms';

import { Adresse, AdresseEingabe } from '../../api';
import { AnschriftFelder, anschriftFormular } from '../anschrift-felder/anschrift-felder';

// I-2: Spiegel der Backend-Regel nur für frühe Rückmeldung; das Backend entscheidet.
function mindestensEinKennzeichen(gruppe: AbstractControl): ValidationErrors | null {
  const { istLieferadresse, istRechnungsadresse } = gruppe.value;
  return istLieferadresse || istRechnungsadresse ? null : { keinKennzeichen: true };
}

@Component({
  selector: 'app-adresse-formular',
  imports: [ReactiveFormsModule, AnschriftFelder],
  template: `
    <form class="formular" [formGroup]="formular" (ngSubmit)="speichern()" novalidate>
      <app-anschrift-felder [gruppe]="formular.controls.anschrift" praefix="adresse-" />

      <label class="auswahl">
        <input type="checkbox" formControlName="istLieferadresse" />
        Lieferadresse
      </label>
      <label class="auswahl">
        <input type="checkbox" formControlName="istRechnungsadresse" />
        Rechnungsadresse
      </label>
      @if (formular.hasError('keinKennzeichen') && formular.touched) {
        <p class="feldfehler">Bitte Liefer- und/oder Rechnungsadresse wählen.</p>
      }

      @if (fehlermeldung()) {
        <p class="hinweis fehler" role="alert">{{ fehlermeldung() }}</p>
      }

      <div class="aktionen">
        <button type="submit" [disabled]="sendet()">Adresse speichern</button>
        <button type="button" class="knopf zweitrangig" (click)="abgebrochen.emit()">
          Abbrechen
        </button>
      </div>
    </form>
  `,
})
export class AdresseFormular implements OnInit {
  private readonly formBuilder = inject(NonNullableFormBuilder);

  readonly adresse = input<Adresse>();
  readonly sendet = input(false);
  readonly fehlermeldung = input('');
  readonly gespeichert = output<AdresseEingabe>();
  readonly abgebrochen = output<void>();

  protected formular = this.neuesFormular();

  ngOnInit(): void {
    this.formular = this.neuesFormular(this.adresse());
  }

  protected speichern(): void {
    if (this.formular.invalid) {
      this.formular.markAllAsTouched();
      return;
    }
    this.gespeichert.emit(this.formular.getRawValue());
  }

  private neuesFormular(adresse?: Adresse) {
    return this.formBuilder.group(
      {
        anschrift: anschriftFormular(this.formBuilder, adresse?.anschrift),
        istLieferadresse: adresse?.istLieferadresse ?? true,
        istRechnungsadresse: adresse?.istRechnungsadresse ?? false,
      },
      { validators: mindestensEinKennzeichen },
    );
  }
}
