import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { errorText, fullName, initials } from '../../format';
import { Role } from '../../models';

interface NavItem {
  label: string;
  link: string;
  exact: boolean;
  icon: string;
  roles?: Role[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, ReactiveFormsModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);
  readonly session = inject(SessionService);
  readonly fullName = fullName;
  readonly initials = initials;

  readonly showPasswordForm = signal(false);
  readonly passwordError = signal('');
  readonly passwordNotice = signal('');
  readonly savingPassword = signal(false);

  readonly passwordForm = this.fb.nonNullable.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(6)]]
  });

  readonly allItems: NavItem[] = [
    { label: 'Overview', link: '/home', exact: true, icon: 'overview' },
    { label: 'New Request', link: '/new-request', exact: false, icon: 'plus' },
    { label: 'Team Calendar', link: '/calendar', exact: false, icon: 'calendar' },
    { label: 'Request History', link: '/request-history', exact: false, icon: 'history' },
    { label: 'Projects', link: '/projects', exact: false, icon: 'projects' },
    { label: 'Approvals', link: '/approvals', exact: false, icon: 'approvals', roles: ['MANAGER', 'ADMIN'] },
    { label: 'Administration', link: '/administration', exact: false, icon: 'admin', roles: ['ADMIN'] },
  ];

  readonly items = computed(() => {
    const role = this.session.role();
    return this.allItems.filter((item) => !item.roles || (role && item.roles.includes(role)));
  });

  togglePasswordForm(): void {
    this.passwordError.set('');
    this.passwordNotice.set('');
    this.passwordForm.reset({ currentPassword: '', newPassword: '' });
    this.showPasswordForm.update((v) => !v);
  }

  savePassword(): void {
    this.passwordForm.markAllAsTouched();
    if (this.passwordForm.invalid || this.savingPassword()) return;

    this.passwordError.set('');
    this.passwordNotice.set('');
    this.savingPassword.set(true);

    const { currentPassword, newPassword } = this.passwordForm.getRawValue();
    this.api.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.savingPassword.set(false);
        this.showPasswordForm.set(false);
        this.passwordNotice.set('Password updated.');
        this.passwordForm.reset({ currentPassword: '', newPassword: '' });
      },
      error: (err) => {
        this.savingPassword.set(false);
        this.passwordError.set(errorText(err));
      }
    });
  }

  logout(): void {
    this.session.logout();
  }
}
