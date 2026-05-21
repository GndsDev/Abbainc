import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EstoqueComponent } from './estoque';

describe('Estoque', () => {
  let component: EstoqueComponent;
  let fixture: ComponentFixture<EstoqueComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EstoqueComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Estoque);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
