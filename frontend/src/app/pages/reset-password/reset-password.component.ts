import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { errorText, generateSecurePassword } from '../../format';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ApiService);
  readonly session = inject(SessionService);

  readonly loading = signal(false);
  readonly error = signal('');
  readonly showPassword = signal(false);

  readonly form = this.fb.nonNullable.group({
    token: ['', [Validators.required]],
    newPassword: ['', [Validators.required, Validators.minLength(6)]]
  });

  ngOnInit(): void {
    const queryToken = this.route.snapshot.queryParamMap.get('token');
    if (queryToken) {
      this.form.controls.token.setValue(queryToken);
    }
  }

  generateRandom(): void {
    const pwd = generateSecurePassword(12);
    this.form.controls.newPassword.setValue(pwd);
    this.showPassword.set(true);
  }

  toggleVisibility(): void {
    this.showPassword.update((v) => !v);
  }

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) return;

    this.error.set('');
    this.loading.set(true);

    const { token, newPassword } = this.form.getRawValue();
    this.api.resetPasswordWithToken(token.trim(), newPassword).subscribe({
      next: () => {
        this.loading.set(false);
        this.session.sessionNotice.set('Your password has been updated. Please sign in with your new password.');
        void this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(errorText(err));
      }
    });
  }
}
