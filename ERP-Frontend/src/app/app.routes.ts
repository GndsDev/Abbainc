import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard';
import { EstoqueComponent } from './pages/estoque/estoque';
import { VendasComponent } from './pages/vendas/vendas';
import { ClientesComponent } from './pages/clientes/clientes';
import { HistoricoComponent } from './pages/historico/historico';

export const routes: Routes = [
  { path: 'dashboard', component: DashboardComponent },
  { path: 'estoque', component: EstoqueComponent },
  { path: 'vendas', component: VendasComponent },
  { path: 'clientes', component: ClientesComponent},
  { path: 'historico', component: HistoricoComponent},
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' }
];
