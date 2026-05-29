import { Component, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterModule, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ToastComponent } from './components/toast/toast';
import { FormsModule } from '@angular/forms';
import { AuthService } from './services/auth';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet, RouterModule, ToastComponent],
  templateUrl: './app.html'
})
export class AppComponent {
  authService = inject(AuthService);
  private router = inject(Router);
  menuAberto = signal(false);
  modoEscuro = signal(false);
  rotaPublica = signal(false);
  usuarioLogin = signal('');
  senhaLogin = signal('');
  erroLogin = signal('');
  autenticando = signal(false);

  constructor() {
    const temaSalvo = localStorage.getItem('tema');
    this.modoEscuro.set(temaSalvo === 'dark');
    this.aplicarTema();
    this.atualizarRotaPublica(this.router.url);
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe(event => this.atualizarRotaPublica(event.urlAfterRedirects));
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

  private atualizarRotaPublica(url: string) {
    const rota = url.split('?')[0].split('#')[0];
    this.rotaPublica.set(rota === '/' || rota === '/checkout');
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
