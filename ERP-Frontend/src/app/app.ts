import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ToastComponent } from './components/toast/toast';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterModule, ToastComponent],
  templateUrl: './app.html'
})
export class AppComponent {
  menuAberto = signal(false);
  modoEscuro = signal(false);

  constructor() {

    const temaSalvo = localStorage.getItem('tema');
    this.modoEscuro.set(temaSalvo === 'dark');
    this.aplicarTema();
  }

  toggleMenu() {
    this.menuAberto.update(v => !v);
  }

  toggleTema() {
    this.modoEscuro.update(v => !v);
    this.aplicarTema();
  }

  private aplicarTema() {
    if (this.modoEscuro()) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('tema', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('tema', 'light');
    }
  }
}
