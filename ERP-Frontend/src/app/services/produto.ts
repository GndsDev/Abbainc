import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Produto } from '../entities/produto.entity';
import { API_BASE_URL } from '../config/api';

@Injectable({
  providedIn: 'root'
})
export class ProdutoService {
  private http = inject(HttpClient);
  private apiUrl = `${API_BASE_URL}/produtos`;

  listarProdutos(): Observable<Produto[]> {
    return this.http.get<Produto[]>(this.apiUrl);
  }

  cadastrarProduto(formData: FormData): Observable<Produto> {
    return this.http.post<Produto>(this.apiUrl, formData);
  }

  atualizarProduto(id: number, formData: FormData): Observable<Produto> {
    return this.http.put<Produto>(`${this.apiUrl}/${id}`, formData);
  }

  excluirVariacao(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/variacoes/${id}`);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
