import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PedidoDTO, Pedido } from '../entities/pedido.entity';
import { API_BASE_URL } from '../config/api';

@Injectable({
  providedIn: 'root'
})
export class PedidoService {
  private http = inject(HttpClient);
  private apiUrl = `${API_BASE_URL}/pedidos`;

  registrarVenda(pedido: PedidoDTO): Observable<any> {
    return this.http.post(this.apiUrl, pedido);
  }

  listarHistorico(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(this.apiUrl);
  }

  baixarReciboPdf(pedidoId: number) {
    return this.http.get(`${this.apiUrl}/${pedidoId}/recibo`, { responseType: 'blob' });
  }

  enviarReciboPorEmail(pedidoId: number) {
    return this.http.post<void>(`${this.apiUrl}/${pedidoId}/recibo/email`, {});
  }

  atualizarStatus(pedidoId: number, status: string): Observable<Pedido> {
    return this.http.patch<Pedido>(`${this.apiUrl}/${pedidoId}/status?status=${status}`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
