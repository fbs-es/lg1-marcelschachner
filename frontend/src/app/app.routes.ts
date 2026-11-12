import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'produkte' },
  {
    path: 'produkte',
    title: 'Produkte',
    loadComponent: () =>
      import('./katalog/produkt-liste/produkt-liste').then((m) => m.ProduktListe),
  },
  {
    path: 'produkte/neu',
    title: 'Produkt anlegen',
    loadComponent: () =>
      import('./katalog/produkt-anlegen/produkt-anlegen').then((m) => m.ProduktAnlegen),
  },
  { path: '**', redirectTo: 'produkte' },
];
