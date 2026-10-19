import { Component, effect, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { AppUser, Balance, Role } from '../../models';
import { errorText, fullName, generateSecurePassword } from '../../format';

@Component({
  selector: 'app-administration',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './administration.component.html',
  styleUrl: './administration.component.scss'
})
export class AdministrationComponent {
  private readonly api = inject(ApiService);
  readonly session = inject(SessionService);
  private readonly fb = inject(FormBuilder);

  readonly error = signal('');
  readonly notice = signal('');
  readonly confirmId = signal<number | null>(null);
  readonly editingUserId = signal<number | null>(null);
  readonly resettingPasswordUserId = signal<number | null>(null);
  readonly showNewUserPassword = signal(false);
  readonly showResetPassword = signal(false);

  readonly selectedUserId = signal<number | null>(null);
  readonly balances = signal<Balance[]>([]);
  readonly loadingBalances = signal(false);
  readonly editingBalanceId = signal<number | null>(null);

  readonly roles: Role[] = ['EMPLOYEE', 'MANAGER', 'ADMIN'];
  readonly fullName = fullName;

  readonly userForm = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.email]],
    password: [''],
    role: ['EMPLOYEE' as Role, Validators.required],
    managerId: [''],
  });

  readonly editUserForm = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    role: ['EMPLOYEE' as Role, Validators.required],
    managerId: [''],
  });

  readonly resetPasswordForm = this.fb.nonNullable.group({
    newPassword: ['', [Validators.required, Validators.minLength(6)]]
  });

  readonly balanceForm = this.fb.nonNullable.group({
    name: ['Vacation', Validators.required],
    maxDays: [20, [Validators.required, Validators.min(1)]],
    daysLeft: [20, [Validators.required, Validators.min(0)]],
  });

  readonly editBalanceForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    maxDays: [1, [Validators.required, Validators.min(1)]],
    daysLeft: [0, [Validators.required, Validators.min(0)]],
  });

  constructor() {
    effect(() => {
      const users = this.session.users();
      const currentSelected = this.selectedUserId();
      if (users.length > 0 && (!currentSelected || !users.some((u) => u.id === currentSelected))) {
        const nextId = this.session.current()?.id ?? users[0].id;
        this.selectUserForBalances(nextId);
      }
    }, { allowSignalWrites: true });
  }

  addPerson(): void {
    this.userForm.markAllAsTouched();
    if (this.userForm.invalid) return;
    this.error.set('');
    this.notice.set('');
    const value = this.userForm.getRawValue();
    const body: {
      firstName: string;
      lastName: string;
      email?: string;
      password?: string;
      role: Role;
      managerId?: number;
    } = {
      firstName: value.firstName.trim(),
      lastName: value.lastName.trim(),
      role: value.role,
    };
    if (value.email.trim()) {
      body.email = value.email.trim();
    }
    if (value.password.trim()) {
      body.password = value.password.trim();
    }
    if (value.managerId) {
      body.managerId = Number(value.managerId);
    }
    this.api.createUser(body).subscribe({
      next: () => {
        this.notice.set('Person added. Welcome email and 24h setup link sent.');
        this.showNewUserPassword.set(false);
        this.userForm.reset({ firstName: '', lastName: '', email: '', password: '', role: 'EMPLOYEE', managerId: '' });
        this.session.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  generateNewUserPassword(): void {
    const pwd = generateSecurePassword(12);
    this.userForm.controls.password.setValue(pwd);
    this.showNewUserPassword.set(true);
  }

  generateResetUserPassword(): void {
    const pwd = generateSecurePassword(12);
    this.resetPasswordForm.controls.newPassword.setValue(pwd);
    this.showResetPassword.set(true);
  }

  startEditUser(user: AppUser): void {
    this.error.set('');
    this.notice.set('');
    this.resettingPasswordUserId.set(null);
    this.editingUserId.set(user.id);
    this.editUserForm.reset({
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email ?? '',
      role: user.role ?? 'EMPLOYEE',
      managerId: user.manager?.id ? String(user.manager.id) : '',
    });
  }

  cancelEditUser(): void {
    this.editingUserId.set(null);
  }

  saveEditUser(userId: number): void {
    this.editUserForm.markAllAsTouched();
    if (this.editUserForm.invalid) return;
    this.error.set('');
    this.notice.set('');
    const value = this.editUserForm.getRawValue();
    const body: {
      firstName: string;
      lastName: string;
      email: string;
      role: Role;
      managerId?: number | null;
      clearManager?: boolean;
    } = {
      firstName: value.firstName.trim(),
      lastName: value.lastName.trim(),
      email: value.email.trim(),
      role: value.role,
    };
    if (value.managerId) {
      body.managerId = Number(value.managerId);
      body.clearManager = false;
    } else {
      body.clearManager = true;
    }

    this.api.updateUser(userId, body).subscribe({
      next: () => {
        this.editingUserId.set(null);
        this.notice.set('User updated.');
        this.session.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  startResetPassword(userId: number): void {
    this.error.set('');
    this.notice.set('');
    this.editingUserId.set(null);
    this.showResetPassword.set(false);
    this.resettingPasswordUserId.set(userId);
    this.resetPasswordForm.reset({ newPassword: '' });
  }

  cancelResetPassword(): void {
    this.showResetPassword.set(false);
    this.resettingPasswordUserId.set(null);
  }

  saveResetPassword(userId: number): void {
    this.resetPasswordForm.markAllAsTouched();
    if (this.resetPasswordForm.invalid) return;
    this.error.set('');
    this.notice.set('');
    const { newPassword } = this.resetPasswordForm.getRawValue();
    this.api.resetUserPassword(userId, newPassword).subscribe({
      next: () => {
        this.showResetPassword.set(false);
        this.resettingPasswordUserId.set(null);
        this.notice.set('User password has been reset and notification email sent.');
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  removePerson(id: number): void {
    this.error.set('');
    this.notice.set('');
    this.api.deleteUser(id).subscribe({
      next: () => {
        this.confirmId.set(null);
        this.notice.set('Person deactivated.');
        this.session.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  selectUserForBalances(userId: number | string): void {
    const id = Number(userId);
    if (!id) return;
    this.selectedUserId.set(id);
    this.editingBalanceId.set(null);
    this.loadBalances(id);
  }

  loadBalances(employeeId = this.selectedUserId()): void {
    if (!employeeId) return;
    this.loadingBalances.set(true);
    this.api.balances(employeeId).subscribe({
      next: (rows) => {
        this.balances.set(rows);
        this.loadingBalances.set(false);
      },
      error: (err) => {
        this.error.set(errorText(err));
        this.loadingBalances.set(false);
      }
    });
  }

  addBalance(): void {
    const employeeId = this.selectedUserId();
    if (!employeeId) return;
    this.balanceForm.markAllAsTouched();
    if (this.balanceForm.invalid) return;
    this.error.set('');
    this.notice.set('');
    const value = this.balanceForm.getRawValue();
    this.api.createBalance({
      employeeId,
      name: value.name.trim(),
      maxDays: Number(value.maxDays),
      daysLeft: Number(value.daysLeft),
    }).subscribe({
      next: () => {
        this.notice.set('Allowance added.');
        this.balanceForm.reset({ name: 'Vacation', maxDays: 20, daysLeft: 20 });
        this.loadBalances(employeeId);
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  startEditBalance(balance: Balance): void {
    this.error.set('');
    this.notice.set('');
    this.editingBalanceId.set(balance.id);
    this.editBalanceForm.reset({
      name: balance.name,
      maxDays: balance.maxDays,
      daysLeft: balance.daysLeft,
    });
  }

  cancelEditBalance(): void {
    this.editingBalanceId.set(null);
  }

  saveEditBalance(balanceId: number): void {
    this.editBalanceForm.markAllAsTouched();
    if (this.editBalanceForm.invalid) return;
    this.error.set('');
    this.notice.set('');
    const value = this.editBalanceForm.getRawValue();
    this.api.updateBalance(balanceId, {
      name: value.name.trim(),
      maxDays: Number(value.maxDays),
      daysLeft: Number(value.daysLeft),
    }).subscribe({
      next: () => {
        this.editingBalanceId.set(null);
        this.notice.set('Allowance updated.');
        this.loadBalances();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  removeBalance(balanceId: number): void {
    this.error.set('');
    this.notice.set('');
    this.api.deleteBalance(balanceId).subscribe({
      next: () => {
        this.notice.set('Allowance removed.');
        this.loadBalances();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  availableManagersFor(userId: number): AppUser[] {
    return this.session.users().filter((u) => u.id !== userId);
  }
}
