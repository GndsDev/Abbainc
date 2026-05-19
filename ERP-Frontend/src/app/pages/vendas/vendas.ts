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
  imports: [CommonModule],
  template: ``
})
export class VendasComponent implements OnInit {
  private camisaService = inject(CamisaService);
  private pedidoService = inject(PedidoService);

  estoqueDisponivel = signal<Camisa[]>([]);
  carrinho = signal<ItemCarrinho[]>([]);

  clienteSelecionado = signal<number>(1);
  formaPagamentoSelecionada = signal<string>('PIX');


  totalCarrinho = computed(() => {
    return this.carrinho().reduce((acc, item) => acc + item.subtotal, 0);
  });

  ngOnInit() {
    this.carregarEstoque();
  }

  carregarEstoque() {
    this.camisaService.listarEstoque().subscribe(dados => {

      this.estoqueDisponivel.set(dados.filter(c => c.quantidadeEmEstoque > 0));
    });
  }

  adicionarAoCarrinho(camisa: Camisa, quantidadeDesejada: number = 1) {
    if (quantidadeDesejada > camisa.quantidadeEmEstoque) {
      alert('Quantidade excede o estoque disponível!');
      return;
    }

    this.carrinho.update(itens => {

      const index = itens.findIndex(i => i.camisa.id === camisa.id);

      if (index !== -1) {

        const itensAtualizados = [...itens];
        const novaQuantidade = itensAtualizados[index].quantidade + quantidadeDesejada;

        if(novaQuantidade > camisa.quantidadeEmEstoque) {
            alert('Estoque insuficiente para adicionar mais dessa camisa.');
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
      alert('O carrinho está vazio.');
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
        alert('Venda finalizada com sucesso! O estoque foi baixado.');
        this.carrinho.set([]);
        this.carregarEstoque();
      },
      error: (erro) => {
        alert('Falha ao processar a venda: ' + (erro.error || erro.message));
      }
    });
  }
}
