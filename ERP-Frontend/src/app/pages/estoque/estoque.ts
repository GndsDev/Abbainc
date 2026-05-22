import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { ProdutoService } from '../../services/produto';
import { ToastService } from '../../services/toast';
import { Produto } from '../../entities/produto.entity';

@Component({
  selector: 'app-estoque',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './estoque.html'
})
export class EstoqueComponent implements OnInit {
  private produtoService = inject(ProdutoService);
  private toastService = inject(ToastService);
  private fb = inject(FormBuilder);

  produtos = signal<Produto[]>([]);
  carregando = signal(true);
  modalAberto = signal(false);
  produtoEditando = signal<Produto | null>(null);
  arquivoSelecionado = signal<File | null>(null);
  nomeArquivoSelecionado = signal('');

  listaTamanhos = ['PP', 'P', 'M', 'G', 'GG', 'XG'];
  tamanhosSelecionados = signal<string[]>([]);
  quantidadesPorTamanho: { [key: string]: number } = {};
  idsPorTamanho: { [key: string]: number } = {};
  termoPesquisa = signal('');

  produtosFiltrados = computed(() => {
    const termo = this.termoPesquisa().toLowerCase();
    const lista = this.produtos();

    if (!termo) return lista;

    return lista.filter(produto =>
      produto.modelo.toLowerCase().includes(termo) ||
      produto.cor.toLowerCase().includes(termo) ||
      produto.variacoes.some(variacao => variacao.sku.toLowerCase().includes(termo))
    );
  });

  formProduto: FormGroup = this.fb.group({
    sku: ['', Validators.required],
    modelo: ['', Validators.required],
    cor: ['', Validators.required],
    preco: [0, [Validators.required, Validators.min(0.1)]]
  });

  ngOnInit() {
    this.carregarEstoque();
  }

  carregarEstoque() {
    this.carregando.set(true);
    this.produtoService.listarProdutos().subscribe({
      next: (dados) => {
        this.produtos.set(dados);
        this.carregando.set(false);
      },
      error: () => {
        this.toastService.mostrar('Erro ao carregar o estoque.', 'erro');
        this.carregando.set(false);
      }
    });
  }

  toggleTamanho(tamanho: string) {
    const atual = this.tamanhosSelecionados();
    if (atual.includes(tamanho)) {
      this.tamanhosSelecionados.set(atual.filter(t => t !== tamanho));
      delete this.quantidadesPorTamanho[tamanho];
      delete this.idsPorTamanho[tamanho];
    } else {
      this.tamanhosSelecionados.set([...atual, tamanho]);
      this.quantidadesPorTamanho[tamanho] = 1;
    }
  }

  abrirModal(produto?: Produto) {
    if (produto) {
      this.produtoEditando.set(produto);
      const skuBase = this.obterSkuBase(produto);

      this.formProduto.patchValue({
        sku: skuBase,
        modelo: produto.modelo,
        cor: produto.cor,
        preco: produto.preco
      });

      this.tamanhosSelecionados.set(produto.variacoes.map(variacao => variacao.tamanho));
      this.quantidadesPorTamanho = {};
      this.idsPorTamanho = {};

      produto.variacoes.forEach(variacao => {
        this.quantidadesPorTamanho[variacao.tamanho] = variacao.quantidadeEmEstoque;
        if (variacao.id) {
          this.idsPorTamanho[variacao.tamanho] = variacao.id;
        }
      });
    } else {
      this.produtoEditando.set(null);
      this.formProduto.reset({ preco: 0 });
      this.tamanhosSelecionados.set([]);
      this.quantidadesPorTamanho = {};
      this.idsPorTamanho = {};
    }

    this.arquivoSelecionado.set(null);
    this.nomeArquivoSelecionado.set('');
    this.modalAberto.set(true);
  }

  fecharModal() {
    this.modalAberto.set(false);
    this.produtoEditando.set(null);
    this.arquivoSelecionado.set(null);
    this.nomeArquivoSelecionado.set('');
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const arquivo = input.files?.[0] || null;

    this.arquivoSelecionado.set(arquivo);
    this.nomeArquivoSelecionado.set(arquivo?.name || '');
  }

  gerarSkuAutomatico() {
    const prefixo = 'ABB-';
    const numeroAleatorio = Math.floor(100000 + Math.random() * 900000);
    const skuGerado = `${prefixo}${numeroAleatorio}`;
    this.formProduto.patchValue({ sku: skuGerado });
    this.toastService.mostrar('Código SKU gerado!', 'info');
  }

  salvarProduto() {
    if (this.formProduto.invalid) {
      this.toastService.mostrar('Preencha os dados base corretamente.', 'erro');
      return;
    }

    const tamanhos = this.tamanhosSelecionados();
    if (tamanhos.length === 0) {
      this.toastService.mostrar('Selecione pelo menos um tamanho para a grade.', 'erro');
      return;
    }

    if (!this.produtoEditando() && !this.arquivoSelecionado()) {
      this.toastService.mostrar('Selecione uma imagem para o produto.', 'erro');
      return;
    }

    const dadosFormulario = this.formProduto.value;
    const produtoDTO = {
      modelo: dadosFormulario.modelo,
      cor: dadosFormulario.cor,
      preco: dadosFormulario.preco,
      variacoes: tamanhos.map(tamanho => ({
        id: this.idsPorTamanho[tamanho],
        tamanho,
        sku: this.criarSkuVariacao(dadosFormulario.sku, tamanho),
        quantidadeEmEstoque: this.quantidadesPorTamanho[tamanho] || 0
      }))
    };

    const formData = new FormData();
    const arquivo = this.arquivoSelecionado();

    if (arquivo) {
      formData.append('imagem', arquivo);
    }

    formData.append('produto', new Blob([JSON.stringify(produtoDTO)], { type: 'application/json' }));

    const produtoAtual = this.produtoEditando();

    if (produtoAtual?.id) {
      this.produtoService.atualizarProduto(produtoAtual.id, formData).subscribe({
        next: () => {
          this.toastService.mostrar('Produto atualizado com sucesso!', 'sucesso');
          this.fecharModal();
          this.carregarEstoque();
        },
        error: (erro) => this.toastService.mostrar('Falha ao atualizar: ' + (erro.error || erro.message), 'erro')
      });
      return;
    }

    this.produtoService.cadastrarProduto(formData).subscribe({
      next: () => {
        this.toastService.mostrar('Produto cadastrado com sucesso!', 'sucesso');
        this.fecharModal();
        this.carregarEstoque();
      },
      error: (erro) => this.toastService.mostrar('Falha ao cadastrar: ' + (erro.error || erro.message), 'erro')
    });
  }

  excluirVariacao(id: number) {
    if (confirm(`Tem certeza que deseja excluir este tamanho do estoque?`)) {
      this.produtoService.excluirVariacao(id).subscribe({
        next: () => {
          this.toastService.mostrar('Tamanho excluído com sucesso!', 'sucesso');
          this.carregarEstoque();
        },
        error: () => this.toastService.mostrar('Erro ao excluir. Pode estar atrelado a uma venda.', 'erro')
      });
    }
  }

  totalEstoque(produto: Produto) {
    return produto.variacoes.reduce((total, variacao) => total + variacao.quantidadeEmEstoque, 0);
  }

  obterSkuBase(produto: Produto) {
    const primeiraVariacao = produto.variacoes[0];
    if (!primeiraVariacao) return '';

    const sufixo = `-${primeiraVariacao.tamanho}`;
    return primeiraVariacao.sku.endsWith(sufixo)
      ? primeiraVariacao.sku.substring(0, primeiraVariacao.sku.lastIndexOf('-'))
      : primeiraVariacao.sku;
  }

  private criarSkuVariacao(skuBase: string, tamanho: string) {
    const skuLimpo = String(skuBase || '').trim().toUpperCase();
    return skuLimpo.endsWith(`-${tamanho}`) ? skuLimpo : `${skuLimpo}-${tamanho}`;
  }
}
