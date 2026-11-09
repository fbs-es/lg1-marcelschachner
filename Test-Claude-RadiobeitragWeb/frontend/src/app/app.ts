import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { Meldungen } from './components/meldungen/meldungen';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, Meldungen],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
