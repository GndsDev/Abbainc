import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { ClienteService } from '../../services/cliente';
import { PedidoService } from '../../services/pedido';
import { ProdutoService } from '../../services/produto';
import { ToastService } from '../../services/toast';
import { VendasComponent } from './vendas';

registerLocaleData(localePt);

describe('VendasComponent', () => {
  let component: VendasComponent;
  let fixture: ComponentFixture<VendasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VendasComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: ProdutoService, useValue: { listarProdutos: () => of([]) } },
        { provide: PedidoService, useValue: { listarHistorico: () => of([]) } },
        { provide: ClienteService, useValue: { listarClientes: () => of([]) } },
        { provide: ToastService, useValue: { mostrar: jasmine.createSpy('mostrar') } },
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(VendasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
