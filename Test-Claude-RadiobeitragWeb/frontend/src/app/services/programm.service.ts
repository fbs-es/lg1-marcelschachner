import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ProgrammDto, SendungDto, SendungRequest } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ProgrammService {
  private readonly http = inject(HttpClient);
  private readonly basis = '/api/programme';

  programmLesen(id: number): Observable<ProgrammDto> {
    return this.http.get<ProgrammDto>(`${this.basis}/${id}`);
  }

  sendungenAuflisten(id: number): Observable<SendungDto[]> {
    return this.http.get<SendungDto[]>(`${this.basis}/${id}/sendungen`);
  }

  sendungAnlegen(id: number, request: SendungRequest): Observable<SendungDto> {
    return this.http.post<SendungDto>(`${this.basis}/${id}/sendungen`, request);
  }
}
