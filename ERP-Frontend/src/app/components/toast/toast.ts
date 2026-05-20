import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed bottom-6 right-6 z-50 flex flex-col gap-3 pointer-events-none">
      @for (toast of toastService.toasts(); track toast.id) {

        <div
          class="pointer-events-auto flex items-center gap-3 px-5 py-3.5 rounded-2xl shadow-2xl border transition-all transform origin-bottom"
          [ngClass]="{
            'bg-green-50 border-green-200 text-green-900 dark:bg-green-950/40 dark:border-green-900 dark:text-green-300': toast.tipo === 'sucesso',
            'bg-red-50 border-red-200 text-red-900 dark:bg-red-950/40 dark:border-red-900 dark:text-red-300': toast.tipo === 'erro',
            'bg-white border-gray-200 text-gray-900 dark:bg-neutral-800 dark:border-neutral-700 dark:text-white': toast.tipo === 'info'
          }">

          <span class="text-sm font-bold tracking-tight">{{ toast.mensagem }}</span>

          <button (click)="toastService.remover(toast.id)" class="opacity-50 hover:opacity-100 transition-opacity p-1">
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>

      }
    </div>
  `
})
export class ToastComponent {
  toastService = inject(ToastService);
}
