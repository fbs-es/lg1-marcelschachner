import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';

import { Adresse, AdresseEingabe, AdressbuchService, Kunde, KundenService } from '../../api';
import { Bestellhistorie } from '../../bestellung/bestellhistorie/bestellhistorie';
import { problemMeldung } from '../../gemeinsam/problem-meldung';
import { AdresseFormular } from '../adresse-formular/adresse-formular';

type Zustand = 'laedt' | 'fehler' | 'geladen';

/** `null`: kein Formular offen, `'neu'`: Adresse hinzufügen, sonst die ID der bearbeiteten Adresse. */
type Bearbeitung = null | 'neu' | string;

@Component({
  selector: 'app-kunde-detail',
  imports: [RouterLink, AdresseFormular, Bestellhistorie],
  templateUrl: './kunde-detail.html',
  styleUrl: './kunde-detail.css',
})
export class KundeDetail implements OnInit {
  private readonly kundenService = inject(KundenService);
  private readonly adressbuchService = inject(AdressbuchService);

  readonly kundenId = input.required<string>();

  protected readonly zustand = signal<Zustand>('laedt');
  protected readonly kunde = signal<Kunde | undefined>(undefined);
  protected readonly fehlermeldung = signal('');
  protected readonly bearbeitung = signal<Bearbeitung>(null);
  protected readonly sendet = signal(false);
  protected readonly aktionsfehler = signal('');

  ngOnInit(): void {
    this.laden();
  }

  protected laden(): void {
    this.zustand.set('laedt');
    this.kundenService.findeKunde(this.kundenId()).subscribe({
      next: (kunde) => {
        this.kunde.set(kunde);
        this.zustand.set('geladen');
      },
      error: (fehler) => {
        this.fehlermeldung.set(problemMeldung(fehler));
        this.zustand.set('fehler');
      },
    });
  }

  protected oeffne(bearbeitung: Bearbeitung): void {
    this.aktionsfehler.set('');
    this.bearbeitung.set(bearbeitung);
  }

  protected speichern(eingabe: AdresseEingabe): void {
    const bearbeitung = this.bearbeitung();
    const anfrage =
      bearbeitung === 'neu'
        ? this.adressbuchService.fuegeAdresseHinzu(this.kundenId(), eingabe)
        : this.adressbuchService.aendereAdresse(this.kundenId(), bearbeitung!, eingabe);
    this.fuehreAus(anfrage, (adresse: Adresse) => {
      this.aendereAdressen((adressen) =>
        bearbeitung === 'neu'
          ? [...adressen, adresse]
          : adressen.map((a) => (a.id === adresse.id ? adresse : a)),
      );
      this.bearbeitung.set(null);
    });
  }

  protected loeschen(adresse: Adresse): void {
    this.bearbeitung.set(null);
    this.fuehreAus(this.adressbuchService.entferneAdresse(this.kundenId(), adresse.id), () =>
      this.aendereAdressen((adressen) => adressen.filter((a) => a.id !== adresse.id)),
    );
  }

  private fuehreAus<T>(anfrage: Observable<T>, beiErfolg: (antwort: T) => void): void {
    this.sendet.set(true);
    this.aktionsfehler.set('');
    anfrage.subscribe({
      next: (antwort) => {
        beiErfolg(antwort);
        this.sendet.set(false);
      },
      error: (fehler) => {
        this.aktionsfehler.set(problemMeldung(fehler));
        this.sendet.set(false);
      },
    });
  }

  private aendereAdressen(aenderung: (adressen: Adresse[]) => Adresse[]): void {
    this.kunde.update((kunde) => kunde && { ...kunde, adressen: aenderung(kunde.adressen) });
  }
}
