import { Routes } from '@angular/router';
import { EstoqueComponent } from './pages/estoque/estoque';
import { VendasComponent } from './pages/vendas/vendas';
import { ClientesComponent } from './pages/clientes/clientes';

export const routes: Routes = [
  { path: 'estoque', component: EstoqueComponent },
  { path: 'vendas', component: VendasComponent },
  {path: 'clientes', component: ClientesComponent},
  { path: '', redirectTo: '/estoque', pathMatch: 'full' }
];
