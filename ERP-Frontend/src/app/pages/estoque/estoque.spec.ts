import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { ProdutoService } from '../../services/produto';
import { ToastService } from '../../services/toast';
import { EstoqueComponent } from './estoque';

describe('EstoqueComponent', () => {
  let component: EstoqueComponent;
  let fixture: ComponentFixture<EstoqueComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EstoqueComponent],
      providers: [
        provideZonelessChangeDetection(),
        {
          provide: ProdutoService,
          useValue: {
            listarProdutos: () => of([]),
          },
        },
        {
          provide: ToastService,
          useValue: {
            mostrar: jasmine.createSpy('mostrar'),
          },
        },
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(EstoqueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
