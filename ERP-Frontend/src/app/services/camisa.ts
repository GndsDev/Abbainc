import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Camisa } from '../entities/camisa.entity';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CamisaService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/camisas';

  listarEstoque(): Observable<Camisa[]> {
    return this.http.get<Camisa[]>(this.apiUrl);
  }

  cadastrarCamisa(camisa: Partial<Camisa>): Observable<Camisa> {
    return this.http.post<Camisa>(this.apiUrl, camisa);
  }

  atualizarCamisa(id: number, camisa: Partial<Camisa>): Observable<Camisa> {
    return this.http.put<Camisa>(`${this.apiUrl}/${id}`, camisa);
  }

  excluirCamisa(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
