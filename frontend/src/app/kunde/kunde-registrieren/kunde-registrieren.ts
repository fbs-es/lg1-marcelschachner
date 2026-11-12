import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { KundenService } from '../../api';
import { problemMeldung } from '../../gemeinsam/problem-meldung';
import { AnschriftFelder, anschriftFormular } from '../anschrift-felder/anschrift-felder';

@Component({
  selector: 'app-kunde-registrieren',
  imports: [ReactiveFormsModule, RouterLink, AnschriftFelder],
  template: `
    <h1>Kunde registrieren</h1>

    <form class="formular" [formGroup]="formular" (ngSubmit)="speichern()" novalidate>
      <label for="vorname">Vorname</label>
      <input id="vorname" formControlName="vorname" maxlength="100" autocomplete="given-name" />
      @if (istUngueltig('vorname')) {
        <p class="feldfehler">Bitte einen Vornamen mit höchstens 100 Zeichen angeben.</p>
      }

      <label for="nachname">Nachname</label>
      <input id="nachname" formControlName="nachname" maxlength="100" autocomplete="family-name" />
      @if (istUngueltig('nachname')) {
        <p class="feldfehler">Bitte einen Nachnamen mit höchstens 100 Zeichen angeben.</p>
      }

      <label for="email">E-Mail</label>
      <input id="email" type="email" formControlName="email" maxlength="254" autocomplete="email" />
      @if (istUngueltig('email')) {
        <p class="feldfehler">Bitte eine gültige E-Mail-Adresse angeben.</p>
      }

      <h2>Lieferadresse</h2>
      <app-anschrift-felder [gruppe]="formular.controls.lieferadresse" />

      <label class="auswahl">
        <input
          id="istAuchRechnungsadresse"
          type="checkbox"
          formControlName="istAuchRechnungsadresse"
        />
        Auch als Rechnungsadresse verwenden
      </label>

      @if (fehlermeldung()) {
        <p class="hinweis fehler" role="alert">{{ fehlermeldung() }}</p>
      }

      <div class="aktionen">
        <button type="submit" [disabled]="sendet()">Registrieren</button>
        <a class="knopf zweitrangig" routerLink="/kunden">Abbrechen</a>
      </div>
    </form>
  `,
  styles: `
    h2 {
      margin: 1.25rem 0 0;
      font-size: 1.1rem;
    }
  `,
})
export class KundeRegistrieren {
  private readonly kundenService = inject(KundenService);
  private readonly router = inject(Router);
  private readonly formBuilder = inject(NonNullableFormBuilder);

  protected readonly formular = this.formBuilder.group({
    vorname: ['', [Validators.required, Validators.maxLength(100)]],
    nachname: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    lieferadresse: anschriftFormular(this.formBuilder),
    istAuchRechnungsadresse: false,
  });

  protected readonly sendet = signal(false);
  protected readonly fehlermeldung = signal('');

  protected speichern(): void {
    if (this.formular.invalid) {
      this.formular.markAllAsTouched();
      return;
    }
    this.sendet.set(true);
    this.fehlermeldung.set('');
    this.kundenService.registriereKunde(this.formular.getRawValue()).subscribe({
      next: (kunde) => this.router.navigate(['/kunden', kunde.id]),
      error: (fehler) => {
        this.fehlermeldung.set(problemMeldung(fehler));
        this.sendet.set(false);
      },
    });
  }

  protected istUngueltig(feld: 'vorname' | 'nachname' | 'email'): boolean {
    const control = this.formular.controls[feld];
    return control.invalid && control.touched;
  }
}
