import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  mensagem: string;
  tipo: 'sucesso' | 'erro' | 'info';
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {

  toasts = signal<Toast[]>([]);
  private contadorId = 0;

  mostrar(mensagem: string, tipo: 'sucesso' | 'erro' | 'info' = 'info') {
    const id = this.contadorId++;
    const novoToast: Toast = { id, mensagem, tipo };

    this.toasts.update(atual => [...atual, novoToast]);

    setTimeout(() => {
      this.remover(id);
    }, 3500);
  }

  remover(id: number) {
    this.toasts.update(atual => atual.filter(t => t.id !== id));
  }
}
