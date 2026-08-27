import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { EMPTY, catchError, concatMap, finalize, from, map, Observable, of, switchMap, tap, toArray } from 'rxjs';

import {
  Produto,
  ProdutoImagem,
  TipoImagemProduto,
} from '../../entities/produto.entity';
import { ProdutoService } from '../../services/produto';
import { ToastService } from '../../services/toast';

interface ImagemPendente {
  id: number;
  arquivo: File;
  previewUrl: string;
  tipo: TipoImagemProduto;
  altText: string;
  principal: boolean;
}

@Component({
  selector: 'app-estoque',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './estoque.html',
  styleUrl: './estoque.scss',
})
export class EstoqueComponent implements OnInit {
  private readonly produtoService = inject(ProdutoService);
  private readonly toastService = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  protected readonly produtos = signal<Produto[]>([]);
  protected readonly carregando = signal(true);
  protected readonly salvando = signal(false);
  protected readonly modalAberto = signal(false);
  protected readonly produtoEditando = signal<Produto | null>(null);
  protected readonly imagensSelecionadas = signal<ImagemPendente[]>([]);
  protected readonly termoPesquisa = signal('');

  protected readonly listaTamanhos = ['PP', 'P', 'M', 'G', 'GG', 'XG'];
  protected readonly tiposImagem: Array<{ value: TipoImagemProduto; label: string }> = [
    { value: 'COSTAS', label: 'Costas' },
    { value: 'FRENTE', label: 'Frente' },
    { value: 'CAPA', label: 'Capa' },
    { value: 'DETALHE', label: 'Detalhe' },
  ];
  protected readonly tamanhosSelecionados = signal<string[]>([]);
  protected quantidadesPorTamanho: Record<string, number> = {};
  protected idsPorTamanho: Record<string, number> = {};

  private proximoIdImagem = 1;

  protected readonly produtosFiltrados = computed(() => {
    const termo = this.termoPesquisa().toLowerCase();
    const lista = this.produtos();

    if (!termo) return lista;

    return lista.filter(
      (produto) =>
        produto.modelo.toLowerCase().includes(termo) ||
        produto.cor.toLowerCase().includes(termo) ||
        produto.variacoes.some((variacao) => variacao.sku.toLowerCase().includes(termo)),
    );
  });

  protected readonly formProduto: FormGroup = this.fb.group({
    sku: ['', Validators.required],
    modelo: ['', Validators.required],
    cor: ['', Validators.required],
    preco: [0, [Validators.required, Validators.min(0.1)]],
  });

  ngOnInit(): void {
    this.carregarEstoque();
  }

  protected carregarEstoque(): void {
    this.carregando.set(true);
    this.produtoService.listarProdutos().subscribe({
      next: (dados) => {
        this.produtos.set(dados);
        this.carregando.set(false);
      },
      error: () => {
        this.toastService.mostrar('Erro ao carregar o estoque.', 'erro');
        this.carregando.set(false);
      },
    });
  }

  protected toggleTamanho(tamanho: string): void {
    const atual = this.tamanhosSelecionados();
    if (atual.includes(tamanho)) {
      this.tamanhosSelecionados.set(atual.filter((item) => item !== tamanho));
      delete this.quantidadesPorTamanho[tamanho];
      delete this.idsPorTamanho[tamanho];
    } else {
      this.tamanhosSelecionados.set([...atual, tamanho]);
      this.quantidadesPorTamanho[tamanho] = 1;
    }
  }

  protected abrirModal(produto?: Produto): void {
    this.limparImagensPendentes();

    if (produto) {
      this.produtoEditando.set(produto);
      this.formProduto.patchValue({
        sku: this.obterSkuBase(produto),
        modelo: produto.modelo,
        cor: produto.cor,
        preco: produto.preco,
      });

      this.tamanhosSelecionados.set(produto.variacoes.map((variacao) => variacao.tamanho));
      this.quantidadesPorTamanho = {};
      this.idsPorTamanho = {};

      produto.variacoes.forEach((variacao) => {
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

    this.modalAberto.set(true);
  }

  protected fecharModal(): void {
    this.modalAberto.set(false);
    this.produtoEditando.set(null);
    this.limparImagensPendentes();
  }

  protected onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const arquivos = Array.from(input.files ?? []);
    input.value = '';

    if (!arquivos.length) return;

    const tiposPermitidos = new Set(['image/png', 'image/jpeg', 'image/webp']);
    const invalidos = arquivos.filter(
      (arquivo) => !tiposPermitidos.has(arquivo.type) || arquivo.size > 8 * 1024 * 1024,
    );
    if (invalidos.length) {
      this.toastService.mostrar('Use imagens PNG, JPG ou WebP com até 8 MB.', 'erro');
    }

    const validos = arquivos.filter(
      (arquivo) => tiposPermitidos.has(arquivo.type) && arquivo.size <= 8 * 1024 * 1024,
    );
    const atuais = this.imagensSelecionadas();
    const vagas = Math.max(0, 8 - atuais.length);
    const modelo = String(this.formProduto.get('modelo')?.value || 'Produto').trim();
    const tiposUsados = new Set<TipoImagemProduto>([
      ...this.imagensExistentes(this.produtoEditando() ?? undefined).map((imagem) => imagem.tipo),
      ...atuais.map((imagem) => imagem.tipo),
    ]);

    const novas = validos.slice(0, vagas).map((arquivo, indice): ImagemPendente => {
      const tipo = this.proximoTipoImagem(tiposUsados);
      tiposUsados.add(tipo);
      const principal = this.imagensExistentes(this.produtoEditando() ?? undefined).length === 0
        && atuais.length === 0
        && indice === 0;

      return {
        id: this.proximoIdImagem++,
        arquivo,
        previewUrl: URL.createObjectURL(arquivo),
        tipo,
        altText: `Camiseta ${modelo} - ${this.rotuloTipo(tipo).toLowerCase()}`,
        principal,
      };
    });

    this.imagensSelecionadas.set([...atuais, ...novas]);
    if (validos.length > vagas) {
      this.toastService.mostrar('É possível enviar até 8 novas imagens por vez.', 'info');
    }
  }

  protected atualizarTipoImagemPendente(id: number, tipo: TipoImagemProduto): void {
    this.imagensSelecionadas.update((imagens) =>
      imagens.map((imagem) => (imagem.id === id ? { ...imagem, tipo } : imagem)),
    );
  }

  protected atualizarAltImagemPendente(id: number, altText: string): void {
    this.imagensSelecionadas.update((imagens) =>
      imagens.map((imagem) => (imagem.id === id ? { ...imagem, altText } : imagem)),
    );
  }

  protected definirPrincipalPendente(id: number): void {
    this.imagensSelecionadas.update((imagens) =>
      imagens.map((imagem) => ({ ...imagem, principal: imagem.id === id })),
    );
  }

  protected removerImagemPendente(id: number): void {
    const imagem = this.imagensSelecionadas().find((item) => item.id === id);
    if (imagem) URL.revokeObjectURL(imagem.previewUrl);
    this.imagensSelecionadas.update((imagens) => imagens.filter((item) => item.id !== id));
  }

  protected moverImagem(produto: Produto, indice: number, direcao: number): void {
    if (!produto.id) return;
    const imagens = this.imagensExistentes(produto);
    const destino = indice + direcao;
    if (destino < 0 || destino >= imagens.length) return;

    [imagens[indice], imagens[destino]] = [imagens[destino], imagens[indice]];
    this.produtoService.reordenarImagens(produto.id, imagens.map((imagem) => imagem.id)).subscribe({
      next: (atualizado) => this.atualizarProdutoLocal(atualizado),
      error: () => this.toastService.mostrar('Não foi possível reordenar as imagens.', 'erro'),
    });
  }

  protected removerImagemExistente(produto: Produto, imagem: ProdutoImagem): void {
    if (!produto.id || !confirm(`Remover a imagem de ${this.rotuloTipo(imagem.tipo)}?`)) return;

    this.produtoService.removerImagem(produto.id, imagem.id).subscribe({
      next: () => {
        const imagens = this.imagensExistentes(produto)
          .filter((item) => item.id !== imagem.id)
          .map((item, indice) => ({ ...item, ordem: indice, principal: indice === 0 }));
        this.atualizarProdutoLocal({ ...produto, imagens, imagemUrl: imagens[0]?.url });
        this.toastService.mostrar('Imagem removida do produto.', 'sucesso');
      },
      error: () => this.toastService.mostrar('Não foi possível remover a imagem.', 'erro'),
    });
  }

  protected gerarSkuAutomatico(): void {
    const numeroAleatorio = Math.floor(100000 + Math.random() * 900000);
    this.formProduto.patchValue({ sku: `ABB-${numeroAleatorio}` });
    this.toastService.mostrar('Código SKU gerado!', 'info');
  }

  protected salvarProduto(): void {
    if (this.formProduto.invalid) {
      this.toastService.mostrar('Preencha os dados base corretamente.', 'erro');
      return;
    }

    const tamanhos = this.tamanhosSelecionados();
    if (tamanhos.length === 0) {
      this.toastService.mostrar('Selecione pelo menos um tamanho para a grade.', 'erro');
      return;
    }

    if (!this.produtoEditando() && this.imagensSelecionadas().length === 0) {
      this.toastService.mostrar('Adicione pelo menos uma imagem ao produto.', 'erro');
      return;
    }

    const dadosFormulario = this.formProduto.value;
    const produtoDTO = {
      modelo: dadosFormulario.modelo,
      cor: dadosFormulario.cor,
      preco: dadosFormulario.preco,
      variacoes: tamanhos.map((tamanho) => ({
        id: this.idsPorTamanho[tamanho],
        tamanho,
        sku: this.criarSkuVariacao(dadosFormulario.sku, tamanho),
        quantidadeEmEstoque: this.quantidadesPorTamanho[tamanho] || 0,
      })),
    };

    const formData = new FormData();
    formData.append('produto', new Blob([JSON.stringify(produtoDTO)], { type: 'application/json' }));

    const produtoAtual = this.produtoEditando();
    const salvar$ = produtoAtual?.id
      ? this.produtoService.atualizarProduto(produtoAtual.id, formData)
      : this.produtoService.cadastrarProduto(formData);
    let registroSalvo = false;

    this.salvando.set(true);
    salvar$
      .pipe(
        tap(() => (registroSalvo = true)),
        switchMap((produto) => this.enviarImagensPendentes(produto)),
        finalize(() => this.salvando.set(false)),
      )
      .subscribe({
        next: () => {
          this.toastService.mostrar(
            produtoAtual ? 'Produto atualizado com sucesso!' : 'Produto cadastrado com sucesso!',
            'sucesso',
          );
          this.fecharModal();
          this.carregarEstoque();
        },
        error: (erro) => {
          const mensagem = registroSalvo
            ? 'Produto salvo, mas uma ou mais imagens não foram enviadas.'
            : `Falha ao salvar: ${erro.error || erro.message || 'erro inesperado'}`;
          this.toastService.mostrar(mensagem, 'erro');
          if (registroSalvo) this.carregarEstoque();
        },
      });
  }

  protected excluirVariacao(id: number): void {
    if (!confirm('Tem certeza que deseja excluir este tamanho do estoque?')) return;

    this.produtoService.excluirVariacao(id).subscribe({
      next: () => {
        this.toastService.mostrar('Tamanho excluído com sucesso!', 'sucesso');
        this.carregarEstoque();
      },
      error: () => this.toastService.mostrar('Erro ao excluir. Pode estar atrelado a uma venda.', 'erro'),
    });
  }

  protected excluirProduto(produto: Produto): void {
    if (!confirm(`Tem certeza que deseja excluir o produto ${produto.modelo}?`)) return;

    this.produtoService
      .delete(produto.id!)
      .pipe(
        catchError((erro) => {
          this.toastService.mostrar(erro.error || erro.message || 'Erro ao excluir produto.', 'erro');
          return EMPTY;
        }),
      )
      .subscribe(() => {
        this.toastService.mostrar('Produto excluído com sucesso!', 'sucesso');
        this.carregarEstoque();
      });
  }

  protected totalEstoque(produto: Produto): number {
    return produto.variacoes.reduce((total, variacao) => total + variacao.quantidadeEmEstoque, 0);
  }

  protected obterSkuBase(produto: Produto): string {
    const primeiraVariacao = produto.variacoes[0];
    if (!primeiraVariacao) return '';

    const sufixo = `-${primeiraVariacao.tamanho}`;
    return primeiraVariacao.sku.endsWith(sufixo)
      ? primeiraVariacao.sku.substring(0, primeiraVariacao.sku.lastIndexOf('-'))
      : primeiraVariacao.sku;
  }

  protected imagemPrincipal(produto: Produto): string | undefined {
    return this.imagensExistentes(produto).find((imagem) => imagem.principal)?.url
      || this.imagensExistentes(produto)[0]?.url
      || produto.imagemUrl;
  }

  protected imagensExistentes(produto?: Produto): ProdutoImagem[] {
    return [...(produto?.imagens ?? [])].sort(
      (primeira, segunda) => primeira.ordem - segunda.ordem || primeira.id - segunda.id,
    );
  }

  protected rotuloTipo(tipo: TipoImagemProduto): string {
    return this.tiposImagem.find((item) => item.value === tipo)?.label ?? tipo;
  }

  private enviarImagensPendentes(produto: Produto): Observable<Produto> {
    const pendentes = this.imagensSelecionadas();
    if (!produto.id || pendentes.length === 0) return of(produto);

    const ordemInicial = this.imagensExistentes(produto).length;
    return from(pendentes).pipe(
      concatMap((imagem, indice) =>
        this.produtoService.adicionarImagem(
          produto.id!,
          imagem.arquivo,
          imagem.tipo,
          imagem.principal ? 0 : ordemInicial + indice,
          imagem.altText,
          imagem.principal,
        ),
      ),
      toArray(),
      map(() => produto),
    );
  }

  private atualizarProdutoLocal(produto: Produto): void {
    this.produtos.update((produtos) =>
      produtos.map((item) => (item.id === produto.id ? produto : item)),
    );
    if (this.produtoEditando()?.id === produto.id) {
      this.produtoEditando.set(produto);
    }
  }

  private proximoTipoImagem(tiposUsados: Set<TipoImagemProduto>): TipoImagemProduto {
    if (!tiposUsados.has('COSTAS')) return 'COSTAS';
    if (!tiposUsados.has('FRENTE')) return 'FRENTE';
    return 'DETALHE';
  }

  private limparImagensPendentes(): void {
    this.imagensSelecionadas().forEach((imagem) => URL.revokeObjectURL(imagem.previewUrl));
    this.imagensSelecionadas.set([]);
  }

  private criarSkuVariacao(skuBase: string, tamanho: string): string {
    const skuLimpo = String(skuBase || '').trim().toUpperCase();
    return skuLimpo.endsWith(`-${tamanho}`) ? skuLimpo : `${skuLimpo}-${tamanho}`;
  }
}
