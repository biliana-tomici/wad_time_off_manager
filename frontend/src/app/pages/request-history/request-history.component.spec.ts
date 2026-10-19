import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RequestHistoryComponent } from './request-history.component';
import { testProviders } from '../../testing/test-providers';
import { TimeRequest } from '../../models';

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

  it('should show Edit, Cancel, and Delete only for allowed statuses and hide Delete for approved/rejected (2.3.4)', () => {
    const pendingReq: TimeRequest = {
      id: 1, balanceId: 1, status: 'PENDING', startDate: '2099-05-04', endDate: '2099-05-05', explanation: 'Pending',
    };
    const upcomingApprovedReq: TimeRequest = {
      id: 2, balanceId: 1, status: 'APPROVED', startDate: '2099-06-01', endDate: '2099-06-02', explanation: 'Approved future',
    };
    const pastApprovedReq: TimeRequest = {
      id: 3, balanceId: 1, status: 'APPROVED', startDate: '2020-01-06', endDate: '2020-01-07', explanation: 'Approved past',
    };
    const rejectedReq: TimeRequest = {
      id: 4, balanceId: 1, status: 'REJECTED', startDate: '2099-07-01', endDate: '2099-07-02', explanation: 'Rejected',
    };

    expect(component.canEdit(pendingReq)).toBeTrue();
    expect(component.canCancel(pendingReq)).toBeTrue();
    expect(component.canDelete(pendingReq)).toBeTrue();

    expect(component.canEdit(upcomingApprovedReq)).toBeFalse();
    expect(component.canCancel(upcomingApprovedReq)).toBeTrue();
    expect(component.canDelete(upcomingApprovedReq)).toBeFalse();

    expect(component.canEdit(pastApprovedReq)).toBeFalse();
    expect(component.canCancel(pastApprovedReq)).toBeFalse();
    expect(component.canDelete(pastApprovedReq)).toBeFalse();

    expect(component.canEdit(rejectedReq)).toBeFalse();
    expect(component.canCancel(rejectedReq)).toBeFalse();
    expect(component.canDelete(rejectedReq)).toBeFalse();

    component.requests.set([upcomingApprovedReq, rejectedReq]);
    fixture.detectChanges();
    const actionButtons = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll('.row-actions button')
    ).map((btn) => btn.textContent?.trim());
    expect(actionButtons).toEqual(['Cancel']);
  });
});
