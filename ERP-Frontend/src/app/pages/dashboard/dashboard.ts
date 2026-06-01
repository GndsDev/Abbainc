import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { API_BASE_URL } from '../../config/api';

interface ProdutoEstoqueCritico {
  variacaoId: number;
  produtoId: number;
  modelo: string;
  cor: string;
  tamanho: string;
  sku: string;
  quantidadeEmEstoque: number;
}

interface DashboardResumo {
  faturamentoMesAtual: number;
  pedidosPendentes: number;
  produtosEstoqueCritico: ProdutoEstoqueCritico[];
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  private http = inject(HttpClient);
  private apiUrl = `${API_BASE_URL}/dashboard/resumo`;

  resumo = signal<DashboardResumo>({
    faturamentoMesAtual: 0,
    pedidosPendentes: 0,
    produtosEstoqueCritico: []
  });

  carregando = signal(true);
  erro = signal('');

  ngOnInit(): void {
    this.carregarResumo();
  }

  carregarResumo(): void {
    this.carregando.set(true);
    this.erro.set('');

    this.http.get<DashboardResumo>(this.apiUrl).subscribe({
      next: resumo => {
        this.resumo.set({
          faturamentoMesAtual: Number(resumo.faturamentoMesAtual || 0),
          pedidosPendentes: Number(resumo.pedidosPendentes || 0),
          produtosEstoqueCritico: resumo.produtosEstoqueCritico || []
        });
        this.carregando.set(false);
      },
      error: erro => {
        this.erro.set(erro.error || erro.message || 'Erro ao carregar o dashboard.');
        this.carregando.set(false);
      }
    });
  }
}
