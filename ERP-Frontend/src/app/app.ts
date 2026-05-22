import { Component, inject, signal } from '@angular/core';
import { RouterOutlet, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ToastComponent } from './components/toast/toast';
import { FormsModule } from '@angular/forms';
import { AuthService } from './services/auth';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet, RouterModule, ToastComponent],
  templateUrl: './app.html'
})
export class AppComponent {
  authService = inject(AuthService);
  menuAberto = signal(false);
  modoEscuro = signal(false);
  usuarioLogin = signal('');
  senhaLogin = signal('');
  erroLogin = signal('');
  autenticando = signal(false);

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

  login() {
    const usuario = this.usuarioLogin().trim();
    const senha = this.senhaLogin();

    if (!usuario || !senha) {
      this.erroLogin.set('Informe usuário e senha.');
      return;
    }

    this.autenticando.set(true);
    this.erroLogin.set('');

    this.authService.login(usuario, senha).subscribe({
      next: () => {
        this.senhaLogin.set('');
        this.autenticando.set(false);
      },
      error: () => {
        this.erroLogin.set('Usuário ou senha inválidos.');
        this.autenticando.set(false);
      }
    });
  }

  logout() {
    this.authService.logout();
    this.menuAberto.set(false);
  }
}
