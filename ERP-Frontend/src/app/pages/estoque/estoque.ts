import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CamisaService } from '../../services/camisa';
import { ToastService } from '../../services/toast';
import { Camisa } from '../../entities/camisa.entity';

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

  termoPesquisa = signal('');

  modalAberto = signal(false);

  formCamisa: FormGroup = this.fb.group({
    sku: ['', Validators.required],
    modelo: ['', Validators.required],
    cor: ['', Validators.required],
    tamanho: ['', Validators.required],
    preco: [0, [Validators.required, Validators.min(0.1)]],
    quantidadeEmEstoque: [0, [Validators.required, Validators.min(0)]],
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

  abrirModal() {
    this.formCamisa.reset({ preco: 0, quantidadeEmEstoque: 0 });
    this.modalAberto.set(true);
  }

  fecharModal() {
    this.modalAberto.set(false);
  }

  salvarCamisa() {
    if (this.formCamisa.invalid) {
      this.toastService.mostrar('Preencha todos os campos corretamente.', 'erro');
      return;
    }

    const novaCamisa = this.formCamisa.value;

    this.camisaService.cadastrarCamisa(novaCamisa).subscribe({
      next: () => {
        this.toastService.mostrar('Camisa cadastrada com sucesso!', 'sucesso');
        this.fecharModal();
        this.carregarEstoque();
      },
      error: (erro) => {
        this.toastService.mostrar('Falha ao cadastrar: ' + (erro.error || erro.message), 'erro');
      }
    });
  }

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
}
