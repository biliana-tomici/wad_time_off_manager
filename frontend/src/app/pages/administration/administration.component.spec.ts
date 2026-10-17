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
    session.users.set([
      { id: 1, firstName: 'Alice', lastName: 'Smith', role: 'ADMIN', manager: null },
      { id: 2, firstName: 'Bob', lastName: 'Johnson', role: 'MANAGER', manager: { id: 1, firstName: 'Alice', lastName: 'Smith' } },
    ]);
    session.select(1);

    fixture = TestBed.createComponent(AdministrationComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('supports editing user role/manager and managing allowances', () => {
    spyOn(api, 'balances').and.returnValue(of([
      { id: 2, employeeId: 2, name: 'Vacation', daysLeft: 10, maxDays: 20 }
    ]));
    const updateUserSpy = spyOn(api, 'updateUser').and.returnValue(of({
      id: 2,
      firstName: 'Bobby',
      lastName: 'Johnson',
      role: 'ADMIN',
      manager: null,
    }));
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
      role: 'ADMIN',
      managerId: '',
    });
    component.saveEditUser(2);
    expect(updateUserSpy).toHaveBeenCalledWith(2, {
      firstName: 'Bobby',
      lastName: 'Johnson',
      role: 'ADMIN',
      clearManager: true,
    });

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
