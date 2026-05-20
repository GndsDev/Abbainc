import { Routes } from '@angular/router';
import { EstoqueComponent } from './pages/estoque/estoque';
import { VendasComponent } from './pages/vendas/vendas';
import { ClientesComponent } from './pages/clientes/clientes';
import { HistoricoComponent } from './pages/historico/historico';

export const routes: Routes = [
  { path: 'estoque', component: EstoqueComponent },
  { path: 'vendas', component: VendasComponent },
  { path: 'clientes', component: ClientesComponent},
  { path: 'historico', component: HistoricoComponent},
  { path: '', redirectTo: '/estoque', pathMatch: 'full' }
];
