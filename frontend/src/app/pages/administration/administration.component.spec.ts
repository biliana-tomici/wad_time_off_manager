import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AdministrationComponent } from './administration.component';
import { testProviders } from '../../testing/test-providers';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';

describe('AdministrationComponent', () => {
  let component: AdministrationComponent;
  let fixture: ComponentFixture<AdministrationComponent>;
  let api: ApiService;
  let session: SessionService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdministrationComponent],
      providers: testProviders,
    }).compileComponents();

    api = TestBed.inject(ApiService);
    session = TestBed.inject(SessionService);
    const adminUser = { id: 1, firstName: 'Alice', lastName: 'Smith', email: 'alice.smith@timeoff.com', role: 'ADMIN' as const, manager: null };
    session.users.set([
      adminUser,
      { id: 2, firstName: 'Bob', lastName: 'Johnson', email: 'bob.johnson@timeoff.com', role: 'MANAGER', manager: { id: 1, firstName: 'Alice', lastName: 'Smith' } },
    ]);
    session.current.set(adminUser);

    fixture = TestBed.createComponent(AdministrationComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('supports editing user role/manager/email, resetting password, and managing allowances', () => {
    spyOn(api, 'balances').and.returnValue(of([
      { id: 2, employeeId: 2, name: 'Vacation', daysLeft: 10, maxDays: 20 }
    ]));
    const updateUserSpy = spyOn(api, 'updateUser').and.returnValue(of({
      id: 2,
      firstName: 'Bobby',
      lastName: 'Johnson',
      email: 'bobby.johnson@timeoff.com',
      role: 'ADMIN',
      manager: null,
    }));
    const resetPasswordSpy = spyOn(api, 'resetUserPassword').and.returnValue(of(void 0));
    const updateBalanceSpy = spyOn(api, 'updateBalance').and.returnValue(of({
      id: 2,
      employeeId: 2,
      name: 'Annual Leave',
      daysLeft: 12,
      maxDays: 25,
    }));

    fixture.detectChanges();

    component.startEditUser(session.users()[1]);
    component.editUserForm.setValue({
      firstName: 'Bobby',
      lastName: 'Johnson',
      email: 'bobby.johnson@timeoff.com',
      role: 'ADMIN',
      managerId: '',
    });
    component.saveEditUser(2);
    expect(updateUserSpy).toHaveBeenCalledWith(2, {
      firstName: 'Bobby',
      lastName: 'Johnson',
      email: 'bobby.johnson@timeoff.com',
      role: 'ADMIN',
      clearManager: true,
    });

    component.startResetPassword(2);
    component.resetPasswordForm.setValue({ newPassword: 'newSecretPassword' });
    component.saveResetPassword(2);
    expect(resetPasswordSpy).toHaveBeenCalledWith(2, 'newSecretPassword');

    component.selectUserForBalances(2);
    component.startEditBalance(component.balances()[0]);
    component.editBalanceForm.setValue({
      name: 'Annual Leave',
      daysLeft: 12,
      maxDays: 25,
    });
    component.saveEditBalance(2);
    expect(updateBalanceSpy).toHaveBeenCalledWith(2, {
      name: 'Annual Leave',
      daysLeft: 12,
      maxDays: 25,
    });
  });
});
