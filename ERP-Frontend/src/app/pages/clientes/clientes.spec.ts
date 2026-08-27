import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { ClienteService } from '../../services/cliente';
import { ToastService } from '../../services/toast';
import { ClientesComponent } from './clientes';

describe('ClientesComponent', () => {
  let component: ClientesComponent;
  let fixture: ComponentFixture<ClientesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientesComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: ClienteService, useValue: { listarClientes: () => of([]) } },
        { provide: ToastService, useValue: { mostrar: jasmine.createSpy('mostrar') } },
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
