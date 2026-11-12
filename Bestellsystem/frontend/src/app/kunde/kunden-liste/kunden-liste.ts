import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { KundeUebersicht, KundenService } from '../../api';
import { problemMeldung } from '../../gemeinsam/problem-meldung';

type Zustand = 'laedt' | 'fehler' | 'geladen';

@Component({
  selector: 'app-kunden-liste',
  imports: [RouterLink],
  template: `
    <div class="titelzeile">
      <h1>Kunden</h1>
      <a class="knopf" routerLink="/kunden/neu">Kunde registrieren</a>
    </div>

    @switch (zustand()) {
      @case ('laedt') {
        <p class="hinweis" role="status">Kunden werden geladen …</p>
      }
      @case ('fehler') {
        <div class="hinweis fehler" role="alert">
          <p class="meldung">{{ fehlermeldung() }}</p>
          <button type="button" (click)="laden()">Erneut versuchen</button>
        </div>
      }
      @default {
        @if (kunden().length === 0) {
          <p class="hinweis">Noch keine Kunden registriert.</p>
        } @else {
          <table>
            <thead>
              <tr>
                <th scope="col">Name</th>
                <th scope="col">E-Mail</th>
              </tr>
            </thead>
            <tbody>
              @for (kunde of kunden(); track kunde.id) {
                <tr>
                  <td>
                    <a [routerLink]="['/kunden', kunde.id]"
                      >{{ kunde.nachname }}, {{ kunde.vorname }}</a
                    >
                  </td>
                  <td>{{ kunde.email }}</td>
                </tr>
              }
            </tbody>
          </table>
        }
      }
    }
  `,
  styles: `
    .meldung {
      margin-top: 0;
    }
  `,
})
export class KundenListe {
  private readonly kundenService = inject(KundenService);

  protected readonly zustand = signal<Zustand>('laedt');
  protected readonly kunden = signal<KundeUebersicht[]>([]);
  protected readonly fehlermeldung = signal('');

  constructor() {
    this.laden();
  }

  protected laden(): void {
    this.zustand.set('laedt');
    this.kundenService.listeKunden().subscribe({
      next: (kunden) => {
        this.kunden.set(kunden);
        this.zustand.set('geladen');
      },
      error: (fehler) => {
        this.fehlermeldung.set(problemMeldung(fehler));
        this.zustand.set('fehler');
      },
    });
  }
}
