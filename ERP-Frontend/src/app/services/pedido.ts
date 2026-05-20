import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PedidoDTO, Pedido } from '../entities/pedido.entity';

@Injectable({
  providedIn: 'root'
})
export class PedidoService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/pedidos';

  registrarVenda(pedido: PedidoDTO): Observable<any> {
    return this.http.post(this.apiUrl, pedido);
  }

  listarHistorico(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(this.apiUrl);
  }
}
