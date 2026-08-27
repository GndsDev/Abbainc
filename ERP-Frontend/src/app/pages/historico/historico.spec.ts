import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { PedidoService } from '../../services/pedido';
import { ToastService } from '../../services/toast';
import { HistoricoComponent } from './historico';

describe('HistoricoComponent', () => {
  let component: HistoricoComponent;
  let fixture: ComponentFixture<HistoricoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HistoricoComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: PedidoService, useValue: { listarHistorico: () => of([]) } },
        { provide: ToastService, useValue: { mostrar: jasmine.createSpy('mostrar') } },
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(HistoricoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
