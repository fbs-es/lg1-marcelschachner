export * from './adressbuch.service';
import { AdressbuchService } from './adressbuch.service';
export * from './bestellungen.service';
import { BestellungenService } from './bestellungen.service';
export * from './kunden.service';
import { KundenService } from './kunden.service';
export * from './mahnwesen.service';
import { MahnwesenService } from './mahnwesen.service';
export * from './produkte.service';
import { ProdukteService } from './produkte.service';
export * from './zahlungen.service';
import { ZahlungenService } from './zahlungen.service';
export const APIS = [
  AdressbuchService,
  BestellungenService,
  KundenService,
  MahnwesenService,
  ProdukteService,
  ZahlungenService,
];
