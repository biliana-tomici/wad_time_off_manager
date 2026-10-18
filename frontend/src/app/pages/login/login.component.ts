import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { errorText } from '../../format';

interface DemoAccount {
  label: string;
  role: string;
  email: string;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly api = inject(ApiService);
  readonly session = inject(SessionService);

  readonly loading = signal(false);
  readonly error = signal('');
  readonly forgotMode = signal(false);
  readonly forgotNotice = signal('');
  readonly issuedResetToken = signal<string | null>(null);

  readonly demoAccounts: DemoAccount[] = [
    { label: 'Alice Smith', role: 'Admin', email: 'alice.smith@timeoff.com' },
    { label: 'Bob Johnson', role: 'Manager', email: 'bob.johnson@timeoff.com' },
    { label: 'Eve Davis', role: 'Employee', email: 'eve.davis@timeoff.com' },
  ];

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  readonly forgotForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]]
  });

  toggleForgotMode(): void {
    this.error.set('');
    this.forgotNotice.set('');
    this.issuedResetToken.set(null);
    this.session.sessionNotice.set('');
    const currentEmail = this.form.controls.email.value;
    if (!this.forgotMode() && currentEmail) {
      this.forgotForm.controls.email.setValue(currentEmail);
    }
    this.forgotMode.update((v) => !v);
  }

  fillDemo(account: DemoAccount): void {
    this.error.set('');
    this.forgotMode.set(false);
    this.session.sessionNotice.set('');
    this.form.setValue({
      email: account.email,
      password: 'password123'
    });
    this.submit();
  }

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) return;

    this.error.set('');
    this.session.sessionNotice.set('');
    this.loading.set(true);

    const { email, password } = this.form.getRawValue();
    this.session.login(email.trim(), password).subscribe({
      next: () => {
        this.loading.set(false);
        void this.router.navigate(['/home']);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(errorText(err));
      }
    });
  }

  submitForgotPassword(): void {
    this.forgotForm.markAllAsTouched();
    if (this.forgotForm.invalid || this.loading()) return;

    this.error.set('');
    this.forgotNotice.set('');
    this.issuedResetToken.set(null);
    this.loading.set(true);

    const { email } = this.forgotForm.getRawValue();
    this.api.forgotPassword(email.trim()).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.forgotNotice.set(res.message);
        if (res.resetToken) {
          this.issuedResetToken.set(res.resetToken);
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(errorText(err));
      }
    });
  }
}
