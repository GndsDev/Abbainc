import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, HostListener, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { environment } from '../../../environments/environment';

interface VariacaoProduto {
  id: number;
  tamanho: string;
  sku: string;
  quantidadeEmEstoque: number;
}

interface Produto {
  id: number;
  modelo: string;
  cor: string;
  preco: number;
  imagemUrl?: string;
  variacoes: VariacaoProduto[];
}

interface ItemCarrinho {
  produtoId: number;
  variacaoProdutoId: number;
  modelo: string;
  cor: string;
  tamanho: string;
  preco: number;
  quantidade: number;
}

interface CheckoutResponse {
  init_point?: string;
  erro?: string;
}

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.scss'
})
export class CheckoutComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  particulas = Array.from({ length: 18 }, (_, index) => index);
  cursor = signal({ x: 0, y: 0 });
  produtos = signal<Produto[]>([]);
  carregandoProdutos = signal(true);
  enviandoPedido = signal(false);
  erro = signal('');
  carrinho = signal<Record<number, ItemCarrinho>>({});

  checkoutForm = new FormGroup({
    nome: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(3)] }),
    telefone: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(10)] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    cpf: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(11)] })
  });

  itensCarrinho = computed(() => Object.values(this.carrinho()));
  quantidadeItens = computed(() => this.itensCarrinho().reduce((total, item) => total + item.quantidade, 0));
  total = computed(() => this.itensCarrinho().reduce((total, item) => total + item.preco * item.quantidade, 0));
  produtoDestaque = computed(() => this.produtos()[0]);

  ngOnInit(): void {
    this.carregarProdutos();
  }

  @HostListener('window:pointermove', ['$event'])
  moverCursor(event: PointerEvent): void {
    this.cursor.set({ x: event.clientX, y: event.clientY });
  }

  carregarProdutos(): void {
    this.carregandoProdutos.set(true);
    this.erro.set('');

    this.http.get<Produto[]>(`${this.apiUrl}/produtos`).subscribe({
      next: produtos => {
        this.produtos.set(produtos);
        this.carregandoProdutos.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar os produtos.');
        this.carregandoProdutos.set(false);
      }
    });
  }

  selecionarTamanho(produto: Produto, variacao: VariacaoProduto): void {
    if (!variacao.id || variacao.quantidadeEmEstoque <= 0) {
      return;
    }

    const quantidadeAtual = this.carrinho()[produto.id]?.quantidade || 1;

    this.carrinho.update(carrinho => ({
      ...carrinho,
      [produto.id]: {
        produtoId: produto.id,
        variacaoProdutoId: variacao.id,
        modelo: produto.modelo,
        cor: produto.cor,
        tamanho: variacao.tamanho,
        preco: Number(produto.preco),
        quantidade: quantidadeAtual
      }
    }));
  }

  alterarQuantidade(produto: Produto, delta: number): void {
    const item = this.carrinho()[produto.id];

    if (!item) {
      return;
    }

    const novaQuantidade = Math.min(item.quantidade + delta, this.estoqueSelecionado(produto));

    this.carrinho.update(carrinho => {
      const atualizado = { ...carrinho };

      if (novaQuantidade <= 0) {
        delete atualizado[produto.id];
        return atualizado;
      }

      atualizado[produto.id] = {
        ...item,
        quantidade: novaQuantidade
      };

      return atualizado;
    });
  }

  itemCarrinho(produto: Produto): ItemCarrinho | undefined {
    return this.carrinho()[produto.id];
  }

  variacaoSelecionada(produto: Produto, variacao: VariacaoProduto): boolean {
    return this.itemCarrinho(produto)?.variacaoProdutoId === variacao.id;
  }

  estoqueSelecionado(produto: Produto): number {
    const item = this.itemCarrinho(produto);
    const variacao = produto.variacoes.find(opcao => opcao.id === item?.variacaoProdutoId);
    return variacao?.quantidadeEmEstoque || 0;
  }

  imagemProduto(produto: Produto): string {
    if (!produto.imagemUrl) {
      return 'produtos/Camisa-abbainc.jpeg';
    }

    if (produto.imagemUrl.startsWith('http')) {
      return produto.imagemUrl;
    }

    if (produto.imagemUrl.startsWith('/')) {
      return `${this.apiUrl.replace(/\/api$/, '')}${produto.imagemUrl}`;
    }

    return produto.imagemUrl;
  }

  formatarCpf(): void {
    const cpf = this.checkoutForm.controls.cpf.value
      .replace(/\D/g, '')
      .slice(0, 11)
      .replace(/^(\d{3})(\d)/, '$1.$2')
      .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
      .replace(/\.(\d{3})(\d)/, '.$1-$2');

    this.checkoutForm.controls.cpf.setValue(cpf, { emitEvent: false });
  }

  formatarTelefone(): void {
    const telefone = this.checkoutForm.controls.telefone.value.replace(/\D/g, '').slice(0, 11);
    const formatado = telefone.length > 10
      ? telefone.replace(/^(\d{2})(\d{5})(\d{0,4}).*/, '($1) $2-$3')
      : telefone.replace(/^(\d{2})(\d{4})(\d{0,4}).*/, '($1) $2-$3');

    this.checkoutForm.controls.telefone.setValue(formatado.trim(), { emitEvent: false });
  }

  finalizarPedido(): void {
    this.checkoutForm.markAllAsTouched();
    this.erro.set('');

    if (this.checkoutForm.invalid || this.itensCarrinho().length === 0 || this.enviandoPedido()) {
      if (this.itensCarrinho().length === 0) {
        this.erro.set('Selecione pelo menos uma camisa e tamanho.');
      }

      return;
    }

    const cliente = this.checkoutForm.getRawValue();
    const payload = {
      cliente: {
        nome: cliente.nome.trim(),
        telefone: cliente.telefone.trim(),
        email: cliente.email.trim(),
        cpf: cliente.cpf.replace(/\D/g, '')
      },
      itens: this.itensCarrinho().map(item => ({
        variacaoProdutoId: item.variacaoProdutoId,
        quantidade: item.quantidade
      }))
    };

    this.enviandoPedido.set(true);

    this.http.post<CheckoutResponse>(`${this.apiUrl}/site/comprar`, payload).subscribe({
      next: resposta => {
        if (resposta.init_point) {
          window.location.href = resposta.init_point;
          return;
        }

        this.erro.set(resposta.erro || 'Não foi possível gerar o pagamento.');
        this.enviandoPedido.set(false);
      },
      error: erro => {
        this.erro.set(erro?.error?.erro || 'Não foi possível finalizar o pedido.');
        this.enviandoPedido.set(false);
      }
    });
  }
}
