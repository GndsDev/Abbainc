import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../services/cliente';
import { Cliente } from '../../entities/cliente.entity';
import { ToastService } from './../../services/toast';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CamisaService } from '../../services/camisa';
import { PedidoService } from '../../services/pedido';
import { Camisa } from '../../entities/camisa.entity';
import { PedidoDTO } from '../../entities/pedido.entity';

interface ItemCarrinho {
  camisa: Camisa;
  quantidade: number;
  subtotal: number;
}

@Component({
  selector: 'app-vendas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vendas.html'
})
export class VendasComponent implements OnInit {
  private camisaService = inject(CamisaService);
  private pedidoService = inject(PedidoService);
  private clienteService = inject(ClienteService);

  clientes = signal<Cliente[]>([]);
  estoqueDisponivel = signal<Camisa[]>([]);
  carrinho = signal<ItemCarrinho[]>([]);

  clienteSelecionado = signal<number>(1);
  formaPagamentoSelecionada = signal<string>('PIX');

  totalCarrinho = computed(() => {
    return this.carrinho().reduce((acc, item) => acc + item.subtotal, 0);
  });
  toastService: ToastService = inject(ToastService);

  ngOnInit() {
    this.carregarEstoque();
    this.carregarClientes();
  }

  carregarClientes() {
    this.clienteService.listarClientes().subscribe(dados => {
      this.clientes.set(dados);
    });
  }

  carregarEstoque() {
    this.camisaService.listarEstoque().subscribe(dados => {

      this.estoqueDisponivel.set(dados.filter(c => c.quantidadeEmEstoque > 0));
    });
  }

  adicionarAoCarrinho(camisa: Camisa, quantidadeDesejada: number = 1) {
    if (quantidadeDesejada > camisa.quantidadeEmEstoque) {
      this.toastService.mostrar('Quantidade excede o estoque disponível!');
      return;
    }

    this.carrinho.update(itens => {
      const index = itens.findIndex(i => i.camisa.id === camisa.id);

      if (index !== -1) {
        const itensAtualizados = [...itens];
        const novaQuantidade = itensAtualizados[index].quantidade + quantidadeDesejada;

        if(novaQuantidade > camisa.quantidadeEmEstoque) {
            this.toastService.mostrar('Estoque insuficiente para adicionar mais dessa camisa.');
            return itens;
        }

        itensAtualizados[index].quantidade = novaQuantidade;
        itensAtualizados[index].subtotal = novaQuantidade * camisa.preco;
        return itensAtualizados;
      }

      return [...itens, {
        camisa,
        quantidade: quantidadeDesejada,
        subtotal: camisa.preco * quantidadeDesejada
      }];
    });
  }

  removerDoCarrinho(camisaId: number) {
    this.carrinho.update(itens => itens.filter(i => i.camisa.id !== camisaId));
  }

  finalizarVenda() {
    if (this.carrinho().length === 0) {
      this.toastService.mostrar('O carrinho está vazio.');
      return;
    }

      if (!this.clienteSelecionado()) {
      this.toastService.mostrar('Selecione um cliente para a venda.', 'erro');
      return;
    }

    const payload: PedidoDTO = {
      cliente: { id: this.clienteSelecionado() },
      formaPagamento: this.formaPagamentoSelecionada(),
      itens: this.carrinho().map(item => ({
        camisa: { id: item.camisa.id! },
        quantidade: item.quantidade
      }))
    };

    this.pedidoService.registrarVenda(payload).subscribe({
      next: () => {
        this.toastService.mostrar('Venda finalizada com sucesso! O estoque foi baixado.');
        this.carrinho.set([]);
        this.carregarEstoque();
      },
      error: (erro) => {
        this.toastService.mostrar('Falha ao processar a venda: ' + (erro.error || erro.message));
      }
    });
  }
}
