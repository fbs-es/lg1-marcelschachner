import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BestellungUebersicht, BestellungenService } from '../../api';
import { GeldPipe } from '../../gemeinsam/geld.pipe';
import { problemMeldung } from '../../gemeinsam/problem-meldung';

type Zustand = 'laedt' | 'fehler' | 'geladen';

/** Bestellhistorie eines Kunden mit Bezahlstatus, Reihenfolge vom Backend (US-04.2, D-19). */
@Component({
  selector: 'app-bestellhistorie',
  imports: [RouterLink, DatePipe, GeldPipe],
  template: `
    <h2>Bestellungen</h2>
    @switch (zustand()) {
      @case ('laedt') {
        <p class="hinweis" role="status">Bestellungen werden geladen …</p>
      }
      @case ('fehler') {
        <div class="hinweis fehler" role="alert">
          <p class="meldung">{{ fehlermeldung() }}</p>
          <button type="button" (click)="laden()">Erneut versuchen</button>
        </div>
      }
      @default {
        @if (bestellungen().length === 0) {
          <p class="hinweis">Noch keine Bestellungen.</p>
        } @else {
          <table>
            <thead>
              <tr>
                <th scope="col">Bestellnummer</th>
                <th scope="col">Bestelldatum</th>
                <th scope="col" class="zahl">Positionen</th>
                <th scope="col" class="zahl">Gesamtwert</th>
                <th scope="col">Bezahlstatus</th>
              </tr>
            </thead>
            <tbody>
              @for (bestellung of bestellungen(); track bestellung.bestellnummer) {
                <tr>
                  <td>
                    <a
                      [routerLink]="[
                        '/kunden',
                        kundenId(),
                        'bestellungen',
                        bestellung.bestellnummer,
                      ]"
                    >
                      {{ bestellung.bestellnummer }}
                    </a>
                  </td>
                  <td>{{ bestellung.bestelldatum | date: 'dd.MM.yyyy' }}</td>
                  <td class="zahl">{{ bestellung.anzahlPositionen }}</td>
                  <td class="zahl">{{ bestellung.gesamtwert | geld }}</td>
                  <td class="bezahlstatus">
                    @if (bestellung.istVollstaendigBezahlt) {
                      bezahlt
                    } @else {
                      offen: {{ bestellung.offenerBetrag | geld }}
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        }
      }
    }
  `,
  styles: `
    h2 {
      margin-top: 2rem;
      font-size: 1.15rem;
    }
  `,
})
export class Bestellhistorie implements OnInit {
  private readonly bestellungenService = inject(BestellungenService);

  readonly kundenId = input.required<string>();

  protected readonly zustand = signal<Zustand>('laedt');
  protected readonly bestellungen = signal<BestellungUebersicht[]>([]);
  protected readonly fehlermeldung = signal('');

  ngOnInit(): void {
    this.laden();
  }

  protected laden(): void {
    this.zustand.set('laedt');
    this.bestellungenService.listeBestellungen(this.kundenId()).subscribe({
      next: (bestellungen) => {
        this.bestellungen.set(bestellungen);
        this.zustand.set('geladen');
      },
      error: (fehler) => {
        this.fehlermeldung.set(problemMeldung(fehler));
        this.zustand.set('fehler');
      },
    });
  }
}
