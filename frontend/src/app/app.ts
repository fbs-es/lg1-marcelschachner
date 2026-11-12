import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="kopf">
      <a class="marke" routerLink="/">Bestellsystem</a>
      <nav aria-label="Hauptnavigation">
        <a routerLink="/produkte" routerLinkActive="aktiv">Katalog</a>
      </nav>
    </header>
    <main class="inhalt">
      <router-outlet />
    </main>
  `,
  styleUrl: './app.css',
})
export class App {}
