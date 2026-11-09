import { Component, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EilmeldungEinfuegenRequest } from '../../models/api.models';
import { formatDauer } from '../../shared/zeit';

@Component({
  selector: 'app-eilmeldung-formular',
  imports: [FormsModule],
  templateUrl: './eilmeldung-formular.html',
})
export class EilmeldungFormular {
  readonly absenden = output<EilmeldungEinfuegenRequest>();
  readonly abbrechen = output<void>();

  protected readonly formatDauer = formatDauer;

  titel = '';
  dauer = 60;
  dringlichkeitsstufe = 5;
  region = '';
  laufzeit = 0;

  senden(): void {
    this.absenden.emit({
      titel: this.titel,
      dauer: this.dauer,
      dringlichkeitsstufe: this.dringlichkeitsstufe,
      region: this.region,
      laufzeit: this.laufzeit,
    });
  }
}
