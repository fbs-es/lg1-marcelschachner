import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Bestellung, BestellungenService } from '../../api';
import { GeldPipe } from '../../gemeinsam/geld.pipe';
import { problemMeldung } from '../../gemeinsam/problem-meldung';
import { Versandpakete } from '../../versand/versandpakete/versandpakete';
import { transaktionsstatusName, zahlungsartName } from '../bezeichnungen';

type Zustand = 'laedt' | 'fehler' | 'geladen';

/**
 * Bestellansicht mit Bezahlstatus (US-05.1, D-19) und Versand (US-07.3); nach dem Checkout
 * zugleich Bestellbestätigung (US-04.1, US-04.2 AK7).
 */
@Component({
  selector: 'app-bestellung-detail',
  imports: [RouterLink, DatePipe, GeldPipe, Versandpakete],
  templateUrl: './bestellung-detail.html',
  styleUrl: './bestellung-detail.css',
})
export class BestellungDetail implements OnInit {
  private readonly bestellungenService = inject(BestellungenService);

  readonly kundenId = input.required<string>();
  readonly bestellnummer = input.required<string>();
  /** Query-Parameter, den der Checkout setzt, um die Bestätigung anzuzeigen. */
  readonly aufgegeben = input<string>();

  protected readonly zustand = signal<Zustand>('laedt');
  protected readonly bestellung = signal<Bestellung | undefined>(undefined);
  protected readonly fehlermeldung = signal('');

  protected readonly zahlungsartName = zahlungsartName;
  protected readonly transaktionsstatusName = transaktionsstatusName;

  ngOnInit(): void {
    this.laden();
  }

  protected laden(): void {
    this.zustand.set('laedt');
    this.bestellungenService.findeBestellung(this.kundenId(), this.bestellnummer()).subscribe({
      next: (bestellung) => {
        this.bestellung.set(bestellung);
        this.zustand.set('geladen');
      },
      error: (fehler) => {
        this.fehlermeldung.set(problemMeldung(fehler));
        this.zustand.set('fehler');
      },
    });
  }
}
