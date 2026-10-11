import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RequestHistoryComponent } from './request-history.component';
import { testProviders } from '../../testing/test-providers';

describe('RequestHistoryComponent', () => {
  let component: RequestHistoryComponent;
  let fixture: ComponentFixture<RequestHistoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RequestHistoryComponent],
      providers: testProviders,
    }).compileComponents();

    fixture = TestBed.createComponent(RequestHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
