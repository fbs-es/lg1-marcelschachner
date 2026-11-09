import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  BeitragRequest,
  EilmeldungEinfuegenRequest,
  EilmeldungErgebnisDto,
  SendeplanDto,
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class SendungService {
  private readonly http = inject(HttpClient);
  private readonly basis = '/api/sendungen';

  sendeplanLesen(id: number): Observable<SendeplanDto> {
    return this.http.get<SendeplanDto>(`${this.basis}/${id}`);
  }

  beitragHinzufuegen(id: number, request: BeitragRequest): Observable<SendeplanDto> {
    return this.http.post<SendeplanDto>(`${this.basis}/${id}/beitraege`, request);
  }

  beitragKuerzen(id: number, beitragId: number, neueDauer: number): Observable<SendeplanDto> {
    return this.http.post<SendeplanDto>(`${this.basis}/${id}/beitraege/${beitragId}/kuerzen`, {
      neueDauer,
    });
  }

  beitragVerschieben(id: number, beitragId: number, neuerIndex: number): Observable<SendeplanDto> {
    return this.http.post<SendeplanDto>(
      `${this.basis}/${id}/beitraege/${beitragId}/verschieben`,
      { neuerIndex },
    );
  }

  beitragLoeschen(id: number, beitragId: number): Observable<SendeplanDto> {
    return this.http.delete<SendeplanDto>(`${this.basis}/${id}/beitraege/${beitragId}`);
  }

  eilmeldungEinfuegen(
    id: number,
    request: EilmeldungEinfuegenRequest,
  ): Observable<EilmeldungErgebnisDto> {
    return this.http.post<EilmeldungErgebnisDto>(`${this.basis}/${id}/eilmeldung`, request);
  }
}
