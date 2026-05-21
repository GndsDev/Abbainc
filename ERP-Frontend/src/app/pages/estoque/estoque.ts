import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { CamisaService } from '../../services/camisa';
import { ToastService } from '../../services/toast';
import { Camisa } from '../../entities/camisa.entity';
import { forkJoin } from 'rxjs';

export interface CamisaAgrupada {
  sku: string;
  modelo: string;
  cor: string;
  preco: number;
  imagemUrl?: string;
  totalEstoque: number;
  grade: { id: number; tamanho: string; quantidadeEmEstoque: number }[];
}

@Component({
  selector: 'app-estoque',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './estoque.html'
})
export class EstoqueComponent implements OnInit {
  private camisaService = inject(CamisaService);
  private toastService = inject(ToastService);
  private fb = inject(FormBuilder);

  camisas = signal<Camisa[]>([]);
  carregando = signal(true);
  modalAberto = signal(false);

  camisaEditando = signal<Camisa | null>(null);

  listaTamanhos = ['PP', 'P', 'M', 'G', 'GG', 'XG'];
  tamanhosSelecionados = signal<string[]>([]);

  quantidadesPorTamanho: { [key: string]: number } = {};

  termoPesquisa = signal('');

  camisasFiltradas = computed(() => {
    const termo = this.termoPesquisa().toLowerCase();
    const lista = this.camisas();

    if (!termo) return lista;

    return lista.filter(camisa =>
      camisa.modelo.toLowerCase().includes(termo) ||
      camisa.sku.toLowerCase().includes(termo) ||
      camisa.cor.toLowerCase().includes(termo)
    );
  });

  camisasAgrupadas = computed(() => {
    const lista = this.camisasFiltradas();
    const mapa = new Map<string, CamisaAgrupada>();

    lista.forEach(camisa => {

      const chave = `${camisa.modelo}-${camisa.cor}`.toLowerCase();

      if (!mapa.has(chave)) {

        const skuBase = camisa.sku.includes('-') ? camisa.sku.substring(0, camisa.sku.lastIndexOf('-')) : camisa.sku;

        mapa.set(chave, {
          sku: skuBase,
          modelo: camisa.modelo,
          cor: camisa.cor,
          preco: camisa.preco,
          imagemUrl: camisa.imagemUrl || 'assets/placeholder.png',
          totalEstoque: 0,
          grade: []
        });
      }

      const grupo = mapa.get(chave)!;
      grupo.grade.push({
        id: camisa.id!,
        tamanho: camisa.tamanho,
        quantidadeEmEstoque: camisa.quantidadeEmEstoque
      });
      grupo.totalEstoque += camisa.quantidadeEmEstoque;
    });

    return Array.from(mapa.values());
  });

  formCamisa: FormGroup = this.fb.group({
    sku: ['', Validators.required],
    modelo: ['', Validators.required],
    cor: ['', Validators.required],
    preco: [0, [Validators.required, Validators.min(0.1)]],
    imagemUrl: ['']
  });

  ngOnInit() {
    this.carregarEstoque();
  }

  carregarEstoque() {
    this.carregando.set(true);
    this.camisaService.listarEstoque().subscribe({
      next: (dados) => {
        this.camisas.set(dados);
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
    } else {
      this.tamanhosSelecionados.set([...atual, tamanho]);
      this.quantidadesPorTamanho[tamanho] = 1;
    }
  }

  abrirModal(camisa?: Camisa) {
    if (camisa) {
      this.camisaEditando.set(camisa);

      let skuLimpo = camisa.sku;
      if (skuLimpo.endsWith(`-${camisa.tamanho}`)) {
        skuLimpo = skuLimpo.substring(0, skuLimpo.lastIndexOf('-'));
      }

      this.formCamisa.patchValue({ ...camisa, sku: skuLimpo });
      this.tamanhosSelecionados.set([camisa.tamanho]);
      this.quantidadesPorTamanho = { [camisa.tamanho]: camisa.quantidadeEmEstoque };
    } else {
      this.camisaEditando.set(null);
      this.formCamisa.reset({ preco: 0, imagemUrl: '' });
      this.tamanhosSelecionados.set([]);
      this.quantidadesPorTamanho = {};
    }
    this.modalAberto.set(true);
  }

  fecharModal() {
    this.modalAberto.set(false);
    this.camisaEditando.set(null);
  }

  gerarSkuAutomatico() {
    const prefixo = 'ABB-';
    const numeroAleatorio = Math.floor(100000 + Math.random() * 900000);
    const skuGerado = `${prefixo}${numeroAleatorio}`;
    this.formCamisa.patchValue({ sku: skuGerado });
    this.toastService.mostrar('Código SKU gerado!', 'info');
  }

  salvarCamisa() {
    if (this.formCamisa.invalid) {
      this.toastService.mostrar('Preencha os dados base corretamente.', 'erro');
      return;
    }

    const tamanhos = this.tamanhosSelecionados();
    if (tamanhos.length === 0) {
      this.toastService.mostrar('Selecione pelo menos um tamanho para a grade.', 'erro');
      return;
    }

    const dadosFormulario = this.formCamisa.value;
    const camisaAtual = this.camisaEditando();

    if (camisaAtual && camisaAtual.id) {
      const tamanhoUnico = tamanhos[0];

      let skuCorrigido = dadosFormulario.sku;
      if (!skuCorrigido.endsWith(`-${tamanhoUnico}`)) {
        skuCorrigido = `${skuCorrigido}-${tamanhoUnico}`;
      }

      const dadosAtualizados = {
        ...dadosFormulario,
        sku: skuCorrigido,
        tamanho: tamanhoUnico,
        quantidadeEmEstoque: this.quantidadesPorTamanho[tamanhoUnico] || 0
      };

      this.camisaService.atualizarCamisa(camisaAtual.id, dadosAtualizados).subscribe({
        next: () => {
          this.toastService.mostrar('Camisa atualizada com sucesso!', 'sucesso');
          this.fecharModal();
          this.carregarEstoque();
        },
        error: () => this.toastService.mostrar('Falha ao atualizar.', 'erro')
      });
    } else {
      this.toastService.mostrar('Salvando grade de tamanhos...', 'info');

      const requisicoes = tamanhos.map(tam => {
        const novaCamisa = {
          ...dadosFormulario,
          sku: `${dadosFormulario.sku}-${tam}`,
          tamanho: tam,
          quantidadeEmEstoque: this.quantidadesPorTamanho[tam] || 0
        };
        return this.camisaService.cadastrarCamisa(novaCamisa);
      });

      forkJoin(requisicoes).subscribe({
        next: () => {
          this.toastService.mostrar(`Grade cadastrada com sucesso!`, 'sucesso');
          this.fecharModal();
          this.carregarEstoque();
        },
        error: () => this.toastService.mostrar('Falha ao cadastrar a grade.', 'erro')
      });
    }
  }

  excluirCamisa(id: number) {
    if (confirm(`Tem certeza que deseja excluir este tamanho do estoque?`)) {
      this.camisaService.excluirCamisa(id).subscribe({
        next: () => {
          this.toastService.mostrar('Tamanho excluído com sucesso!', 'sucesso');
          this.carregarEstoque();
        },
        error: () => this.toastService.mostrar('Erro ao excluir. Pode estar atrelado a uma venda.', 'erro')
      });
    }
  }
}
