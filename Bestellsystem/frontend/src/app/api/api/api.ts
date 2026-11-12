export * from './adressbuch.service';
import { AdressbuchService } from './adressbuch.service';
export * from './bestellungen.service';
import { BestellungenService } from './bestellungen.service';
export * from './gutscheine.service';
import { GutscheineService } from './gutscheine.service';
export * from './kunden.service';
import { KundenService } from './kunden.service';
export * from './mahnwesen.service';
import { MahnwesenService } from './mahnwesen.service';
export * from './merkliste.service';
import { MerklisteService } from './merkliste.service';
export * from './produkte.service';
import { ProdukteService } from './produkte.service';
export * from './versand.service';
import { VersandService } from './versand.service';
export * from './zahlungen.service';
import { ZahlungenService } from './zahlungen.service';
export const APIS = [
  AdressbuchService,
  BestellungenService,
  GutscheineService,
  KundenService,
  MahnwesenService,
  MerklisteService,
  ProdukteService,
  VersandService,
  ZahlungenService,
];
