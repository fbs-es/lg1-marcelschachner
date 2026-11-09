import { Component, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  BEITRAG_TYPEN,
  BeitragRequest,
  BeitragTyp,
  MUSIKGENRES,
  Musikgenre,
} from '../../models/api.models';

@Component({
  selector: 'app-beitrag-formular',
  imports: [FormsModule],
  templateUrl: './beitrag-formular.html',
})
export class BeitragFormular {
  readonly absenden = output<BeitragRequest>();

  protected readonly typen = BEITRAG_TYPEN;
  protected readonly genres = MUSIKGENRES;

  typ: BeitragTyp = 'MUSIK';
  titel = '';
  dauer = 180;
  kuenstler = '';
  album = '';
  genre: Musikgenre = 'POP';
  kunde = '';
  werbebudget = 0;
  sprechertext = '';
  dringlichkeitsstufe = 3;
  region = '';

  erstelleRequest(): BeitragRequest {
    const basis = { titel: this.titel, dauer: this.dauer };
    switch (this.typ) {
      case 'MUSIK':
        return { typ: 'MUSIK', ...basis, kuenstler: this.kuenstler, album: this.album, genre: this.genre };
      case 'WERBUNG':
        return { typ: 'WERBUNG', ...basis, kunde: this.kunde, werbebudget: this.werbebudget };
      case 'MODERATION':
        return { typ: 'MODERATION', ...basis, sprechertext: this.sprechertext };
      case 'EILMELDUNG':
        return {
          typ: 'EILMELDUNG',
          ...basis,
          dringlichkeitsstufe: this.dringlichkeitsstufe,
          region: this.region,
        };
    }
  }

  senden(): void {
    this.absenden.emit(this.erstelleRequest());
  }
}
