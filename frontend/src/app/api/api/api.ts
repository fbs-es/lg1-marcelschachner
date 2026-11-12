export * from './adressbuch.service';
import { AdressbuchService } from './adressbuch.service';
export * from './kunden.service';
import { KundenService } from './kunden.service';
export * from './produkte.service';
import { ProdukteService } from './produkte.service';
export const APIS = [AdressbuchService, KundenService, ProdukteService];
