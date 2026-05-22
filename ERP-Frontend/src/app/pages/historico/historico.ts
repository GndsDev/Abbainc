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

  imprimirRecibo(pedido: Pedido) {
    this.toastService.mostrar('Gerando recibo...', 'info');

    this.pedidoService.baixarReciboPdf(pedido.id!).subscribe({
      next: (arquivoBlob) => {
        const url = window.URL.createObjectURL(arquivoBlob);

        const link = document.createElement('a');
        link.href = url;
        link.download = `Recibo_Abbainc_Pedido_${pedido.id}.pdf`;
        link.click();

        window.URL.revokeObjectURL(url);
        this.toastService.mostrar('Recibo baixado com sucesso!', 'sucesso');
      },
      error: () => {
        this.toastService.mostrar('Erro ao gerar o recibo.', 'erro');
      }
    });
  }

  enviarReciboPorEmail(pedido: Pedido) {
    this.toastService.mostrar('Enviando recibo por e-mail...', 'info');

    this.pedidoService.enviarReciboPorEmail(pedido.id!).subscribe({
      next: () => {
        this.toastService.mostrar('Recibo enviado com sucesso!', 'sucesso');
      },
      error: (erro) => {
        this.toastService.mostrar('Erro ao enviar o recibo: ' + (erro.error || erro.message), 'erro');
      }
    });
  }
}
