import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeComponent } from './home.component';
import { testProviders } from '../../testing/test-providers';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: testProviders,
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render read-only allowances without Add or Remove buttons (2.1.1)', () => {
    component.balances.set([
      { id: 1, employeeId: 2, name: 'Vacation', daysLeft: 15, maxDays: 20 },
    ]);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Vacation');
    expect(el.textContent).not.toContain('Add allowance');
    expect(el.textContent).not.toContain('Remove');
  });

  it('should count both PENDING and EDITED requests in Waiting count (2.1.2)', () => {
    component.requests.set([
      { id: 1, balanceId: 1, status: 'PENDING', startDate: '2026-10-19', endDate: '2026-10-20', explanation: 'A' },
      { id: 2, balanceId: 1, status: 'EDITED', startDate: '2026-10-21', endDate: '2026-10-22', explanation: 'B' },
      { id: 3, balanceId: 1, status: 'APPROVED', startDate: '2026-10-26', endDate: '2026-10-27', explanation: 'C' },
    ]);
    expect(component.pendingCount()).toBe(2);
    expect(component.approvedCount()).toBe(1);
  });

  it('should derive working days reserved by PENDING and EDITED requests per balance (2.1.3)', () => {
    const balance = { id: 1, employeeId: 2, name: 'Vacation', daysLeft: 15, maxDays: 20 };
    component.balances.set([balance]);
    // 2026-10-16 is Friday, 2026-10-19 is Monday -> 2 working days
    // 2026-10-21 is Wednesday -> 1 working day
    component.requests.set([
      { id: 1, balanceId: 1, status: 'PENDING', startDate: '2026-10-16', endDate: '2026-10-19', explanation: 'Trip' },
      { id: 2, balanceId: 1, status: 'EDITED', startDate: '2026-10-21', endDate: '2026-10-21', explanation: 'Errand' },
      { id: 3, balanceId: 1, status: 'APPROVED', startDate: '2026-10-12', endDate: '2026-10-13', explanation: 'Past' },
    ]);
    fixture.detectChanges();

    expect(component.reservedDays(balance)).toBe(3);
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('3 reserved by pending requests');
  });
});
