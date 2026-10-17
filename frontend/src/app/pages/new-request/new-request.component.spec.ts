import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NewRequestComponent } from './new-request.component';
import { testProviders } from '../../testing/test-providers';
import { errorText } from '../../format';

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

  it('should calculate working days (Mon-Fri) in preview (2.2.2)', () => {
    // 2026-10-16 (Fri) to 2026-10-19 (Mon) -> 2 working days
    component.form.patchValue({ startDate: '2026-10-16', endDate: '2026-10-19' });
    expect(component.days()).toBe(2);
  });

  it('should compute availableDays as daysLeft minus pending reserved days and warn without saying server will store it (2.2.1, 2.2.3)', () => {
    const balance = { id: 7, employeeId: 2, name: 'Vacation', daysLeft: 5, maxDays: 20 };
    component.balances.set([balance]);
    // 3 working days already pending (Mon-Wed)
    component.requests.set([
      { id: 10, balanceId: 7, status: 'PENDING', startDate: '2026-10-19', endDate: '2026-10-21', explanation: 'Pending' },
    ]);
    // Requesting 3 more working days (Thu, Fri, Mon) -> 3 > (5 - 3 = 2 available)
    component.form.patchValue({ balanceId: '7', startDate: '2026-10-22', endDate: '2026-10-26' });
    fixture.detectChanges();

    expect(component.availableDays(balance)).toBe(2);
    expect(component.overBalance()).toBeTrue();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Exceeds available allowance');
    expect(el.textContent).not.toContain('The server will still store it');
  });

  it('should extract ProblemDetail message in errorText (2.2.3)', () => {
    const httpErr = {
      status: 409,
      error: {
        title: 'Business Rule Violation',
        status: 409,
        detail: 'Requested leave overlaps with existing request id 1',
      },
    };
    expect(errorText(httpErr)).toBe('Requested leave overlaps with existing request id 1');
  });
});
