import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Produto, ProdutoImagem, TipoImagemProduto } from '../entities/produto.entity';
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

  adicionarImagem(
    produtoId: number,
    arquivo: File,
    tipo: TipoImagemProduto,
    ordem: number,
    altText: string,
    principal: boolean,
  ): Observable<ProdutoImagem> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    formData.append('tipo', tipo);
    formData.append('ordem', String(ordem));
    formData.append('altText', altText);
    formData.append('principal', String(principal));
    return this.http.post<ProdutoImagem>(`${this.apiUrl}/${produtoId}/imagens`, formData);
  }

  removerImagem(produtoId: number, imagemId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${produtoId}/imagens/${imagemId}`);
  }

  reordenarImagens(produtoId: number, imagemIds: number[]): Observable<Produto> {
    return this.http.patch<Produto>(`${this.apiUrl}/${produtoId}/imagens/ordem`, { imagemIds });
  }

  excluirVariacao(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/variacoes/${id}`);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
