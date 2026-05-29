import { Routes } from '@angular/router';
import { EstoqueComponent } from './pages/estoque/estoque';
import { VendasComponent } from './pages/vendas/vendas';
import { ClientesComponent } from './pages/clientes/clientes';
import { HistoricoComponent } from './pages/historico/historico';
import { CheckoutComponent } from './pages/checkout/checkout.component';

export const routes: Routes = [
  { path: '', component: CheckoutComponent },
  { path: 'checkout', component: CheckoutComponent },
  { path: 'estoque', component: EstoqueComponent },
  { path: 'vendas', component: VendasComponent },
  { path: 'clientes', component: ClientesComponent},
  { path: 'historico', component: HistoricoComponent}
];
