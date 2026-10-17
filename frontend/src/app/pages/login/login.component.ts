import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
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
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  readonly session = inject(SessionService);

  readonly loading = signal(false);
  readonly error = signal('');

  readonly demoAccounts: DemoAccount[] = [
    { label: 'Alice Smith', role: 'Admin', email: 'alice.smith@timeoff.com' },
    { label: 'Bob Johnson', role: 'Manager', email: 'bob.johnson@timeoff.com' },
    { label: 'Eve Davis', role: 'Employee', email: 'eve.davis@timeoff.com' },
  ];

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  fillDemo(account: DemoAccount): void {
    this.error.set('');
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
}
