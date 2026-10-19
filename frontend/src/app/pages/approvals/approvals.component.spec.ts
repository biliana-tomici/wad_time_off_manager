import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ApprovalsComponent } from './approvals.component';
import { testProviders } from '../../testing/test-providers';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';

describe('ApprovalsComponent', () => {
  let component: ApprovalsComponent;
  let fixture: ComponentFixture<ApprovalsComponent>;
  let api: ApiService;
  let session: SessionService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApprovalsComponent],
      providers: testProviders,
    }).compileComponents();

    api = TestBed.inject(ApiService);
    session = TestBed.inject(SessionService);
    session.users.set([
      { id: 1, firstName: 'Alice', lastName: 'Smith', role: 'ADMIN', manager: null },
      { id: 2, firstName: 'Bob', lastName: 'Johnson', role: 'MANAGER', manager: { id: 1, firstName: 'Alice', lastName: 'Smith' } },
    ]);
    session.current.set(session.users()[0]);

    fixture = TestBed.createComponent(ApprovalsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('renders coverage warnings and sends optional decision comment on approve/reject', () => {
    spyOn(api, 'pending').and.returnValue(of([
      {
        id: 10,
        employeeId: 2,
        employeeName: 'Bob Johnson',
        balanceId: 2,
        balanceName: 'Vacation',
        status: 'PENDING',
        startDate: '2026-11-02',
        endDate: '2026-11-06',
        workingDays: 5,
        explanation: 'Family trip',
        coverageWarnings: ['Approving leaves Project Beta with 2 of 3 required'],
      }
    ]));
    const approveSpy = spyOn(api, 'approveRequest').and.callFake((id, comment) => of({
      id,
      status: 'APPROVED',
      startDate: '2026-11-02',
      endDate: '2026-11-06',
      explanation: 'Family trip',
      decisionComment: comment ?? null,
    }));

    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Approving leaves Project Beta with 2 of 3 required');

    component.setComment(10, 'Approved with coverage note');
    component.decide(component.pending()[0], 'APPROVED');
    expect(approveSpy).toHaveBeenCalledWith(10, 'Approved with coverage note');
  });
});
