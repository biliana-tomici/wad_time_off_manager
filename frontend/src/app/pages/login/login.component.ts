import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { errorText } from '../../format';

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
    this.session.sessionNotice.set('');
    const currentEmail = this.form.controls.email.value;
    if (!this.forgotMode() && currentEmail) {
      this.forgotForm.controls.email.setValue(currentEmail);
    }
    this.forgotMode.update((v) => !v);
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
    this.loading.set(true);

    const { email } = this.forgotForm.getRawValue();
    this.api.forgotPassword(email.trim()).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.forgotNotice.set(res.message);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(errorText(err));
      }
    });
  }
}
