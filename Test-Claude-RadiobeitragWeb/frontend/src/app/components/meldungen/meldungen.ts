import { Component, inject } from '@angular/core';
import { MeldungService } from '../../services/meldung.service';

@Component({
  selector: 'app-meldungen',
  templateUrl: './meldungen.html',
  styleUrl: './meldungen.css',
})
export class Meldungen {
  protected readonly meldungService = inject(MeldungService);
}
