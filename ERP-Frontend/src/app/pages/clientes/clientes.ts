import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClienteService } from '../../services/cliente';
import { ToastService } from '../../services/toast';
import { Cliente } from '../../entities/cliente.entity';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './clientes.html'
})
export class ClientesComponent implements OnInit {
  private clienteService = inject(ClienteService);
  private toastService = inject(ToastService);
  private fb = inject(FormBuilder);

  clientes = signal<Cliente[]>([]);
  carregando = signal(true);
  modalAberto = signal(false);

  clienteIdEmEdicao = signal<number | null>(null);

  formCliente: FormGroup = this.fb.group({
    nome: ['', Validators.required],
    whatsapp: ['', Validators.required],
    endereco: ['']
  });

  ngOnInit() {
    this.carregarClientes();
  }

  carregarClientes() {
    this.carregando.set(true);
    this.clienteService.listarClientes().subscribe({
      next: (dados) => {
        this.clientes.set(dados);
        this.carregando.set(false);
      },
      error: () => {
        this.toastService.mostrar('Erro ao carregar os clientes.', 'erro');
        this.carregando.set(false);
      }
    });
  }

  abrirModal() {
    this.formCliente.reset();
    this.clienteIdEmEdicao.set(null);
    this.modalAberto.set(true);
  }

  abrirModalEdicao(cliente: Cliente) {
    this.clienteIdEmEdicao.set(cliente.id!);

    this.formCliente.patchValue({
      nome: cliente.nome,
      whatsapp: cliente.whatsapp,
      endereco: cliente.endereco
    });

    this.modalAberto.set(true);
  }

  fecharModal() {
    this.modalAberto.set(false);
  }

  salvarCliente() {
    if (this.formCliente.invalid) {
      this.toastService.mostrar('Preencha os campos obrigatórios corretamente.', 'erro');
      return;
    }

    const dadosFormulario = this.formCliente.value;
    const idEdicao = this.clienteIdEmEdicao();

    if (idEdicao) {
      this.clienteService.atualizar(idEdicao, dadosFormulario).subscribe({
        next: () => {
          this.toastService.mostrar('Cliente atualizado com sucesso!', 'sucesso');
          this.fecharModal();
          this.carregarClientes();
        },
        error: (erro) => {
          this.toastService.mostrar('Falha ao atualizar: ' + (erro.error || erro.message), 'erro');
        }
      });
    }
    else {
      this.clienteService.cadastrarCliente(dadosFormulario).subscribe({
        next: () => {
          this.toastService.mostrar('Cliente cadastrado com sucesso!', 'sucesso');
          this.fecharModal();
          this.carregarClientes();
        },
        error: (erro) => {
          this.toastService.mostrar('Falha ao cadastrar: ' + (erro.error || erro.message), 'erro');
        }
      });
    }
  }
}
