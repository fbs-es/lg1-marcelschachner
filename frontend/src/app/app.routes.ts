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
  {
    path: 'kunden',
    title: 'Kunden',
    loadComponent: () => import('./kunde/kunden-liste/kunden-liste').then((m) => m.KundenListe),
  },
  {
    path: 'kunden/neu',
    title: 'Kunde registrieren',
    loadComponent: () =>
      import('./kunde/kunde-registrieren/kunde-registrieren').then((m) => m.KundeRegistrieren),
  },
  {
    path: 'kunden/:kundenId',
    title: 'Kunde',
    loadComponent: () => import('./kunde/kunde-detail/kunde-detail').then((m) => m.KundeDetail),
  },
  { path: '**', redirectTo: 'produkte' },
];
