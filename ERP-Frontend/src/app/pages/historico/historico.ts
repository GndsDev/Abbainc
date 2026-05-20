import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PedidoService } from '../../services/pedido';
import { ToastService } from '../../services/toast';
import { Pedido } from '../../entities/pedido.entity';

@Component({
  selector: 'app-historico',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './historico.html'
})
export class HistoricoComponent implements OnInit {
  private pedidoService = inject(PedidoService);
  private toastService = inject(ToastService);

  pedidos = signal<Pedido[]>([]);
  carregando = signal(true);

  ngOnInit() {
    this.carregarHistorico();
  }

  carregarHistorico() {
    this.carregando.set(true);
    this.pedidoService.listarHistorico().subscribe({
      next: (dados) => {
        this.pedidos.set(dados.reverse());
        this.carregando.set(false);
      },
      error: () => {
        this.toastService.mostrar('Erro ao carregar o histórico de vendas.', 'erro');
        this.carregando.set(false);
      }
    });
  }
}
