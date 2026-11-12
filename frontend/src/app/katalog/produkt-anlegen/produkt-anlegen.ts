import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ProdukteService } from '../../api';
import { euroBetrag, euroInCent } from '../../gemeinsam/euro-eingabe';
import { problemMeldung } from '../../gemeinsam/problem-meldung';

@Component({
  selector: 'app-produkt-anlegen',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './produkt-anlegen.html',
  styleUrl: './produkt-anlegen.css',
})
export class ProduktAnlegen {
  private readonly produkteService = inject(ProdukteService);
  private readonly router = inject(Router);

  protected readonly formular = inject(NonNullableFormBuilder).group({
    titel: ['', [Validators.required, Validators.maxLength(200)]],
    listenpreis: ['', [Validators.required, euroBetrag]],
    lagerbestand: [0, [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]],
  });

  protected readonly sendet = signal(false);
  protected readonly fehlermeldung = signal('');

  protected speichern(): void {
    if (this.formular.invalid) {
      this.formular.markAllAsTouched();
      return;
    }
    const werte = this.formular.getRawValue();
    this.sendet.set(true);
    this.fehlermeldung.set('');
    this.produkteService
      .legeProduktAn({
        titel: werte.titel,
        listenpreis: { cent: euroInCent(werte.listenpreis)!, waehrung: 'EUR' },
        lagerbestand: Number(werte.lagerbestand),
      })
      .subscribe({
        next: () => this.router.navigate(['/produkte']),
        error: (fehler) => {
          this.fehlermeldung.set(problemMeldung(fehler));
          this.sendet.set(false);
        },
      });
  }

  protected istUngueltig(feld: 'titel' | 'listenpreis' | 'lagerbestand'): boolean {
    const control = this.formular.controls[feld];
    return control.invalid && control.touched;
  }
}
