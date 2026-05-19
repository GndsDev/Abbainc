import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CamisaService } from '../../services/camisa';
import { Camisa } from '../../entities/camisa.entity';

@Component({
  selector: 'app-estoque',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './estoque.html'
})
export class EstoqueComponent implements OnInit {

  private camisaService = inject(CamisaService);


  camisas = signal<Camisa[]>([]);
  carregando = signal<boolean>(true);

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
      error: (erro) => {
        console.error('Erro ao buscar estoque', erro);
        this.carregando.set(false);
      }
    });
  }
}
