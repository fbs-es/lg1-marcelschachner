import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BeitragFormular } from '../../components/beitrag-formular/beitrag-formular';
import { EilmeldungFormular } from '../../components/eilmeldung-formular/eilmeldung-formular';
import {
  BEITRAG_TYPEN,
  BeitragDto,
  BeitragRequest,
  BeitragTyp,
  EilmeldungEinfuegenRequest,
  SendeplanDto,
} from '../../models/api.models';
import { MeldungService } from '../../services/meldung.service';
import { SendungService } from '../../services/sendung.service';
import { formatDauer, formatUhrzeit, formatUhrzeitKurz } from '../../shared/zeit';

@Component({
  selector: 'app-sendeplan',
  imports: [FormsModule, RouterLink, BeitragFormular, EilmeldungFormular],
  templateUrl: './sendeplan.html',
  styleUrl: './sendeplan.css',
})
export class Sendeplan implements OnInit {
  private readonly sendungService = inject(SendungService);
  private readonly meldungen = inject(MeldungService);

  readonly id = input.required<string>();

  protected readonly formatDauer = formatDauer;
  protected readonly formatUhrzeit = formatUhrzeit;
  protected readonly formatUhrzeitKurz = formatUhrzeitKurz;

  readonly plan = signal<SendeplanDto | null>(null);
  readonly warnung = signal<string | null>(null);
  readonly kuerzenId = signal<number | null>(null);
  readonly eilmeldungOffen = signal(false);
  kuerzenDauer = 0;

  readonly fortschritt = computed(() => {
    const plan = this.plan();
    if (!plan) {
      return 0;
    }
    return Math.min(100, (plan.gesamtdauer / plan.maximaldauer) * 100);
  });

  ngOnInit(): void {
    this.sendungService.sendeplanLesen(this.sendungId()).subscribe({
      next: (plan) => this.plan.set(plan),
      error: (fehler) => this.meldungen.fehler(fehler),
    });
  }

  typText(typ: BeitragTyp): string {
    return BEITRAG_TYPEN.find((t) => t.wert === typ)?.text ?? typ;
  }

  details(beitrag: BeitragDto): string {
    switch (beitrag.typ) {
      case 'MUSIK':
        return `${beitrag.kuenstler} – ${beitrag.album} (${beitrag.genre})`;
      case 'WERBUNG':
        return `${beitrag.kunde}`;
      case 'MODERATION':
        return beitrag.sprechertext ?? '';
      case 'EILMELDUNG':
        return `Stufe ${beitrag.dringlichkeitsstufe} · ${beitrag.region}`;
    }
  }

  beitragHinzufuegen(request: BeitragRequest): void {
    this.sendungService.beitragHinzufuegen(this.sendungId(), request).subscribe({
      next: (plan) => {
        this.plan.set(plan);
        this.meldungen.erfolg(`„${request.titel}“ wurde hinzugefügt.`);
      },
      error: (fehler) => this.meldungen.fehler(fehler),
    });
  }

  verschieben(beitrag: BeitragDto, neuerIndex: number): void {
    this.sendungService.beitragVerschieben(this.sendungId(), beitrag.id, neuerIndex).subscribe({
      next: (plan) => this.plan.set(plan),
      error: (fehler) => this.meldungen.fehler(fehler),
    });
  }

  loeschen(beitrag: BeitragDto): void {
    this.sendungService.beitragLoeschen(this.sendungId(), beitrag.id).subscribe({
      next: (plan) => {
        this.plan.set(plan);
        this.meldungen.erfolg(`„${beitrag.titel}“ wurde gelöscht.`);
      },
      error: (fehler) => this.meldungen.fehler(fehler),
    });
  }

  kuerzenStarten(beitrag: BeitragDto): void {
    this.kuerzenId.set(beitrag.id);
    this.kuerzenDauer = beitrag.dauer - 1;
  }

  kuerzenBestaetigen(beitrag: BeitragDto): void {
    this.sendungService.beitragKuerzen(this.sendungId(), beitrag.id, this.kuerzenDauer).subscribe({
      next: (plan) => {
        this.plan.set(plan);
        this.kuerzenId.set(null);
      },
      error: (fehler) => this.meldungen.fehler(fehler),
    });
  }

  eilmeldungEinfuegen(request: EilmeldungEinfuegenRequest): void {
    this.sendungService.eilmeldungEinfuegen(this.sendungId(), request).subscribe({
      next: (ergebnis) => {
        this.plan.set(ergebnis.sendeplan);
        this.warnung.set(ergebnis.warnung);
        this.eilmeldungOffen.set(false);
        if (ergebnis.warnung) {
          this.meldungen.warnung(ergebnis.warnung);
        } else {
          this.meldungen.erfolg(`Eilmeldung „${request.titel}“ wurde eingefügt.`);
        }
      },
      error: (fehler) => this.meldungen.fehler(fehler),
    });
  }

  private sendungId(): number {
    return Number(this.id());
  }
}
