import { Routes } from '@angular/router';
import { ProgrammUebersicht } from './pages/programm-uebersicht/programm-uebersicht';
import { Sendeplan } from './pages/sendeplan/sendeplan';

export const routes: Routes = [
  { path: '', component: ProgrammUebersicht, title: 'Programmübersicht – Test-Claude-Radiobeitrag' },
  { path: 'sendungen/:id', component: Sendeplan, title: 'Sendeplan – Test-Claude-Radiobeitrag' },
  { path: '**', redirectTo: '' },
];
