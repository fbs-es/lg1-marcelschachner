import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { SenderDto, SenderRequest } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class SenderService {
  private readonly http = inject(HttpClient);
  private readonly basis = '/api/sender';

  senderAuflisten(): Observable<SenderDto[]> {
    return this.http.get<SenderDto[]>(this.basis);
  }

  senderLesen(id: number): Observable<SenderDto> {
    return this.http.get<SenderDto>(`${this.basis}/${id}`);
  }

  senderAnlegen(request: SenderRequest): Observable<SenderDto> {
    return this.http.post<SenderDto>(this.basis, request);
  }
}
