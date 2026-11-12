import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { Warenkorb } from './bestellung/warenkorb';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="kopf">
      <a class="marke" routerLink="/">Bestellsystem</a>
      <nav aria-label="Hauptnavigation">
        <a routerLink="/produkte" routerLinkActive="aktiv">Katalog</a>
        <a routerLink="/kunden" routerLinkActive="aktiv">Kunden</a>
        <a routerLink="/checkout" routerLinkActive="aktiv"
          >Warenkorb ({{ warenkorb.stueckzahl() }})</a
        >
      </nav>
    </header>
    <main class="inhalt">
      <router-outlet />
    </main>
  `,
  styleUrl: './app.css',
})
export class App {
  protected readonly warenkorb = inject(Warenkorb);
}
