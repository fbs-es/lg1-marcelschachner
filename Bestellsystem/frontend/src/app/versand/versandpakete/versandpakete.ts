import { DatePipe } from '@angular/common';
import { Component, inject, input, linkedSignal, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable } from 'rxjs';

import { PaketAnlegen, VersandService, Versandpaket } from '../../api';
import { problemMeldung } from '../../gemeinsam/problem-meldung';
import { DIENSTLEISTER, dienstleisterName, lieferstatusName } from '../bezeichnungen';

/**
 * Abschnitt „Versand“ der Bestellansicht: Pakete mit Status sehen (US-07.3), anlegen (US-07.1)
 * und Status fortschreiben (US-07.2). Erlaubte Folgestatus liefert das Backend (D-22).
 */
@Component({
  selector: 'app-versandpakete',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './versandpakete.html',
  styleUrl: './versandpakete.css',
})
export class Versandpakete {
  private readonly versandService = inject(VersandService);

  readonly kundenId = input.required<string>();
  readonly bestellnummer = input.required<string>();
  readonly pakete = input.required<Versandpaket[]>();

  protected readonly liste = linkedSignal(() => this.pakete());
  protected readonly aktionsfehler = signal('');
  protected readonly sendet = signal(false);

  protected readonly DIENSTLEISTER = DIENSTLEISTER;
  protected readonly dienstleisterName = dienstleisterName;
  protected readonly lieferstatusName = lieferstatusName;

  protected readonly formular = new FormGroup({
    dienstleister: new FormControl<PaketAnlegen.DienstleisterEnum | ''>('', {
      nonNullable: true,
      validators: Validators.required,
    }),
    sendungsnummer: new FormControl('', {
      nonNullable: true,
      validators: Validators.required,
    }),
  });
  protected readonly dienstleister = this.formular.controls.dienstleister;
  protected readonly sendungsnummer = this.formular.controls.sendungsnummer;

  protected anlegen(): void {
    if (this.formular.invalid) {
      this.formular.markAllAsTouched();
      return;
    }
    const { dienstleister, sendungsnummer } = this.formular.getRawValue();
    this.fuehreAus(
      this.versandService.legePaketAn(this.kundenId(), this.bestellnummer(), {
        dienstleister: dienstleister as PaketAnlegen.DienstleisterEnum,
        sendungsnummer,
      }),
      (paket) => {
        this.liste.update((pakete) => [...pakete, paket]);
        this.formular.reset();
      },
    );
  }

  protected setzeStatus(paket: Versandpaket, status: string): void {
    if (!status) {
      return;
    }
    this.fuehreAus(
      this.versandService.aendereLieferstatus(this.kundenId(), this.bestellnummer(), paket.id, {
        lieferstatus: status as Versandpaket.LieferstatusEnum,
      }),
      (geaendert) =>
        this.liste.update((pakete) => pakete.map((p) => (p.id === geaendert.id ? geaendert : p))),
    );
  }

  private fuehreAus(
    anfrage: Observable<Versandpaket>,
    danach: (paket: Versandpaket) => void,
  ): void {
    this.aktionsfehler.set('');
    this.sendet.set(true);
    anfrage.subscribe({
      next: (paket) => {
        this.sendet.set(false);
        danach(paket);
      },
      error: (fehler) => {
        this.sendet.set(false);
        this.aktionsfehler.set(problemMeldung(fehler));
      },
    });
  }
}
