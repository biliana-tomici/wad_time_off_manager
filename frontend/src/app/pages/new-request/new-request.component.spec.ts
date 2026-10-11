import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NewRequestComponent } from './new-request.component';
import { testProviders } from '../../testing/test-providers';

describe('NewRequestComponent', () => {
  let component: NewRequestComponent;
  let fixture: ComponentFixture<NewRequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewRequestComponent],
      providers: testProviders,
    }).compileComponents();

    fixture = TestBed.createComponent(NewRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
