import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { map, tap } from 'rxjs';
import { API_BASE_URL } from '../config/api';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private token = signal<string | null>(null);

  autenticado = signal(false);
  usuario = signal('');

  get authorizationHeader() {
    const token = this.token();
    return token ? `Basic ${token}` : null;
  }

  login(usuario: string, senha: string) {
    const token = btoa(`${usuario}:${senha}`);
    const headers = new HttpHeaders({ Authorization: `Basic ${token}` });

    return this.http.get<{ usuario: string }>(`${API_BASE_URL}/auth/me`, { headers }).pipe(
      tap((resposta) => {
        this.token.set(token);
        this.usuario.set(resposta.usuario);
        this.autenticado.set(true);
      }),
      map(() => undefined)
    );
  }

  logout() {
    this.token.set(null);
    this.usuario.set('');
    this.autenticado.set(false);
  }
}
