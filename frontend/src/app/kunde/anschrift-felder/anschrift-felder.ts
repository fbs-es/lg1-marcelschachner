import { Component, input } from '@angular/core';
import {
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { Anschrift } from '../../api';

export type AnschriftFormular = FormGroup<{
  strasse: FormControl<string>;
  stadt: FormControl<string>;
  postleitzahl: FormControl<string>;
  land: FormControl<string>;
}>;

/** Feldregeln wie im Backend (D-16), damit Fehler schon vor dem Senden sichtbar sind. */
export function anschriftFormular(
  formBuilder: NonNullableFormBuilder,
  anschrift?: Anschrift,
): AnschriftFormular {
  return formBuilder.group({
    strasse: [anschrift?.strasse ?? '', [Validators.required, Validators.maxLength(200)]],
    stadt: [anschrift?.stadt ?? '', [Validators.required, Validators.maxLength(100)]],
    postleitzahl: [anschrift?.postleitzahl ?? '', [Validators.required, Validators.maxLength(20)]],
    land: [anschrift?.land ?? 'DE', [Validators.required, Validators.pattern(/^[A-Za-z]{2}$/)]],
  });
}

type Feld = 'strasse' | 'stadt' | 'postleitzahl' | 'land';

@Component({
  selector: 'app-anschrift-felder',
  imports: [ReactiveFormsModule],
  template: `
    <ng-container [formGroup]="gruppe()">
      <label [for]="praefix() + 'strasse'">Straße und Hausnummer</label>
      <input [id]="praefix() + 'strasse'" formControlName="strasse" autocomplete="street-address" />
      @if (istUngueltig('strasse')) {
        <p class="feldfehler">Bitte eine Straße mit höchstens 200 Zeichen angeben.</p>
      }

      <label [for]="praefix() + 'postleitzahl'">Postleitzahl</label>
      <input
        [id]="praefix() + 'postleitzahl'"
        formControlName="postleitzahl"
        autocomplete="postal-code"
      />
      @if (istUngueltig('postleitzahl')) {
        <p class="feldfehler">Bitte eine Postleitzahl mit höchstens 20 Zeichen angeben.</p>
      }

      <label [for]="praefix() + 'stadt'">Stadt</label>
      <input [id]="praefix() + 'stadt'" formControlName="stadt" autocomplete="address-level2" />
      @if (istUngueltig('stadt')) {
        <p class="feldfehler">Bitte eine Stadt mit höchstens 100 Zeichen angeben.</p>
      }

      <label [for]="praefix() + 'land'">Land (ISO-Code, z. B. DE)</label>
      <input
        [id]="praefix() + 'land'"
        formControlName="land"
        maxlength="2"
        autocomplete="country"
      />
      @if (istUngueltig('land')) {
        <p class="feldfehler">Bitte einen zweistelligen Ländercode wie DE angeben.</p>
      }
    </ng-container>
  `,
  styles: `
    :host {
      display: contents;
    }
  `,
})
export class AnschriftFelder {
  readonly gruppe = input.required<AnschriftFormular>();
  readonly praefix = input('');

  protected istUngueltig(feld: Feld): boolean {
    const control = this.gruppe().controls[feld];
    return control.invalid && control.touched;
  }
}
