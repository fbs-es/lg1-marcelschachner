import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProgrammDto, SenderDto, SendungDto } from '../../models/api.models';
import { MeldungService } from '../../services/meldung.service';
import { ProgrammService } from '../../services/programm.service';
import { SenderService } from '../../services/sender.service';
import { formatDatum, formatDauer, formatUhrzeitKurz, sekundenAusUhrzeit } from '../../shared/zeit';

@Component({
  selector: 'app-programm-uebersicht',
  imports: [FormsModule, RouterLink],
  templateUrl: './programm-uebersicht.html',
  styleUrl: './programm-uebersicht.css',
})
export class ProgrammUebersicht implements OnInit {
  private readonly senderService = inject(SenderService);
  private readonly programmService = inject(ProgrammService);
  private readonly meldungen = inject(MeldungService);

  protected readonly formatDatum = formatDatum;
  protected readonly formatDauer = formatDauer;
  protected readonly formatUhrzeitKurz = formatUhrzeitKurz;
  protected readonly stunden = [0, 3, 6, 9, 12, 15, 18, 21, 24];

  readonly sender = signal<SenderDto[]>([]);
  readonly ausgewaehlt = signal<SenderDto | null>(null);
  readonly programm = signal<ProgrammDto | null>(null);
  readonly sendungen = computed(() => this.programm()?.sendungen ?? []);

  senderName = '';
  senderFrequenz = 98.5;
  senderDatum = new Date().toISOString().substring(0, 10);

  sendungName = '';
  sendungModerator = 'Marcel Schachner';
  sendungStart = '08:00';
  sendungEnde = '09:00';
  sendungMaximaldauer = 3000;

  ngOnInit(): void {
    this.senderLaden();
  }

  senderLaden(): void {
    this.senderService.senderAuflisten().subscribe({
      next: (liste) => {
        this.sender.set(liste);
        if (!this.ausgewaehlt() && liste.length > 0) {
          this.senderWaehlen(liste[0]);
        }
      },
      error: (fehler) => this.meldungen.fehler(fehler),
    });
  }

  senderWaehlen(sender: SenderDto): void {
    this.ausgewaehlt.set(sender);
    this.programmLaden();
  }

  senderWaehlenNachId(id: string): void {
    const sender = this.sender().find((s) => s.id === Number(id));
    if (sender) {
      this.senderWaehlen(sender);
    }
  }

  programmLaden(): void {
    const sender = this.ausgewaehlt();
    if (!sender) {
      return;
    }
    this.programmService.programmLesen(sender.programmId).subscribe({
      next: (programm) => this.programm.set(programm),
      error: (fehler) => this.meldungen.fehler(fehler),
    });
  }

  senderAnlegen(): void {
    const request = { name: this.senderName, frequenz: this.senderFrequenz, datum: this.senderDatum };
    this.senderService.senderAnlegen(request).subscribe({
      next: (sender) => {
        this.sender.update((liste) => [...liste, sender]);
        this.senderWaehlen(sender);
        this.senderName = '';
        this.meldungen.erfolg(`Sender „${sender.name}“ wurde angelegt.`);
      },
      error: (fehler) => this.meldungen.fehler(fehler),
    });
  }

  sendungAnlegen(): void {
    const sender = this.ausgewaehlt();
    if (!sender) {
      return;
    }
    const request = {
      name: this.sendungName,
      moderator: this.sendungModerator,
      startzeit: this.sendungStart,
      endzeit: this.sendungEnde,
      maximaldauer: this.sendungMaximaldauer,
    };
    this.programmService.sendungAnlegen(sender.programmId, request).subscribe({
      next: (sendung) => {
        this.programmLaden();
        this.sendungName = '';
        this.meldungen.erfolg(`Sendung „${sendung.name}“ wurde angelegt.`);
      },
      error: (fehler) => this.meldungen.fehler(fehler),
    });
  }

  linksProzent(sendung: SendungDto): number {
    return (sekundenAusUhrzeit(sendung.startzeit) / 86400) * 100;
  }

  breiteProzent(sendung: SendungDto): number {
    const dauer = sekundenAusUhrzeit(sendung.endzeit) - sekundenAusUhrzeit(sendung.startzeit);
    return (dauer / 86400) * 100;
  }
}
