import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../services/cliente';
import { Cliente } from '../../entities/cliente.entity';
import { ToastService } from './../../services/toast';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProdutoService } from '../../services/produto';
import { PedidoService } from '../../services/pedido';
import { ItemEstoque, Produto } from '../../entities/produto.entity';
import { Pedido, PedidoDTO } from '../../entities/pedido.entity';
import { EMPTY, catchError } from 'rxjs';

interface ItemCarrinho {
  item: ItemEstoque;
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
  private produtoService = inject(ProdutoService);
  private pedidoService = inject(PedidoService);
  private clienteService = inject(ClienteService);

  clientes = signal<Cliente[]>([]);
  estoqueDisponivel = signal<ItemEstoque[]>([]);
  carrinho = signal<ItemCarrinho[]>([]);
  pedidos = signal<Pedido[]>([]);
  carregandoPedidos = signal(true);
  statusPedidos = ['PENDENTE', 'PAGO', 'ENVIADO', 'ENTREGUE', 'CANCELADO'];

  clienteSelecionado = signal<number>(1);
  formaPagamentoSelecionada = signal<string>('PIX');

  totalCarrinho = computed(() => {
    return this.carrinho().reduce((acc, item) => acc + item.subtotal, 0);
  });
  toastService: ToastService = inject(ToastService);

  ngOnInit() {
    this.carregarEstoque();
    this.carregarClientes();
    this.carregarPedidos();
  }

  carregarClientes() {
    this.clienteService.listarClientes().subscribe(dados => {
      this.clientes.set(dados);
    });
  }

  carregarEstoque() {
    this.produtoService.listarProdutos().subscribe(dados => {
      this.estoqueDisponivel.set(this.criarItensEstoque(dados).filter(item => item.quantidadeEmEstoque > 0));
    });
  }

  carregarPedidos() {
    this.carregandoPedidos.set(true);
    this.pedidoService.listarHistorico().subscribe({
      next: pedidos => {
        this.pedidos.set([...pedidos].reverse());
        this.carregandoPedidos.set(false);
      },
      error: erro => {
        this.toastService.mostrar('Erro ao carregar pedidos: ' + (erro.error || erro.message), 'erro');
        this.carregandoPedidos.set(false);
      }
    });
  }

  adicionarAoCarrinho(item: ItemEstoque, quantidadeDesejada: number = 1) {
    if (quantidadeDesejada > item.quantidadeEmEstoque) {
      this.toastService.mostrar('Quantidade excede o estoque disponível!');
      return;
    }

    this.carrinho.update(itens => {
      const index = itens.findIndex(i => i.item.id === item.id);

      if (index !== -1) {
        const itensAtualizados = [...itens];
        const novaQuantidade = itensAtualizados[index].quantidade + quantidadeDesejada;

        if (novaQuantidade > item.quantidadeEmEstoque) {
          this.toastService.mostrar('Estoque insuficiente para adicionar mais desse produto.');
          return itens;
        }

        itensAtualizados[index].quantidade = novaQuantidade;
        itensAtualizados[index].subtotal = novaQuantidade * item.preco;
        return itensAtualizados;
      }

      return [...itens, {
        item,
        quantidade: quantidadeDesejada,
        subtotal: item.preco * quantidadeDesejada
      }];
    });
  }

  removerDoCarrinho(variacaoId: number) {
    this.carrinho.update(itens => itens.filter(i => i.item.id !== variacaoId));
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
        variacaoProduto: { id: item.item.id! },
        quantidade: item.quantidade
      }))
    };

    this.pedidoService.registrarVenda(payload).subscribe({
      next: () => {
        this.toastService.mostrar('Venda finalizada com sucesso! O estoque foi baixado.');
        this.carrinho.set([]);
        this.carregarEstoque();
        this.carregarPedidos();
      },
      error: (erro) => {
        this.toastService.mostrar('Falha ao processar a venda: ' + (erro.error || erro.message));
      }
    });
  }

  atualizarStatusPedido(pedido: Pedido, status: string) {
    const statusNormalizado = status === 'CANCELADO' ? 'CANCELADO' : status;

    this.pedidoService.atualizarStatus(pedido.id!, statusNormalizado).pipe(
      catchError(erro => {
        this.toastService.mostrar(erro.error || erro.message || 'Erro ao atualizar status do pedido.', 'erro');
        return EMPTY;
      })
    ).subscribe(() => {
      this.toastService.mostrar('Status atualizado com sucesso!', 'sucesso');
      this.carregarPedidos();
      if (statusNormalizado === 'CANCELADO') {
        this.carregarEstoque();
      }
    });
  }

  excluirPedido(pedido: Pedido) {
    if (!confirm(`Deseja excluir o pedido #${pedido.id}?`)) {
      return;
    }

    this.pedidoService.delete(pedido.id!).pipe(
      catchError(erro => {
        this.toastService.mostrar(erro.error || erro.message || 'Erro ao excluir pedido.', 'erro');
        return EMPTY;
      })
    ).subscribe(() => {
      this.toastService.mostrar('Pedido excluído com sucesso!', 'sucesso');
      this.carregarPedidos();
    });
  }

  private criarItensEstoque(produtos: Produto[]) {
    return produtos.flatMap(produto =>
      produto.variacoes.map(variacao => ({
        id: variacao.id,
        produtoId: produto.id,
        modelo: produto.modelo,
        cor: produto.cor,
        tamanho: variacao.tamanho,
        sku: variacao.sku,
        quantidadeEmEstoque: variacao.quantidadeEmEstoque,
        preco: produto.preco,
        imagemUrl: produto.imagemUrl
      }))
    );
  }
}
