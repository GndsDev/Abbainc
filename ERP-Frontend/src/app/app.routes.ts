import { Routes } from '@angular/router';
import { EstoqueComponent } from './pages/estoque/estoque';
import { VendasComponent } from './pages/vendas/vendas';

export const routes: Routes = [
  { path: 'estoque', component: EstoqueComponent },
  { path: 'vendas', component: VendasComponent },
  { path: '', redirectTo: '/estoque', pathMatch: 'full' }
];
