import { TestBed } from '@angular/core/testing';

import { Camisa } from './camisa';

describe('Camisa', () => {
  let service: Camisa;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Camisa);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
