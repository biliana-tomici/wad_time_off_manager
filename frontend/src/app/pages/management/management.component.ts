import { Component, effect, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { TimeRequest } from '../../models';
import { errorText, fullName, prettyDate, statusLabel } from '../../format';

@Component({
  selector: 'app-management',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './management.component.html',
  styleUrl: './management.component.scss'
})
export class ManagementComponent {
  private readonly api = inject(ApiService);
  readonly session = inject(SessionService);
  private readonly fb = inject(FormBuilder);

  readonly pending = signal<TimeRequest[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly confirmId = signal<number | null>(null);
  readonly fullName = fullName;
  readonly prettyDate = prettyDate;
  readonly statusLabel = statusLabel;

  readonly form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    managerId: [''],
  });

  constructor() {
    effect(() => {
      const id = this.session.current()?.id;
      if (!id) {
        this.pending.set([]);
        return;
      }
      this.load(id);
    }, { allowSignalWrites: true });
  }

  load(id = this.session.current()?.id): void {
    if (!id) return;
    this.loading.set(true);
    this.api.pending(id).subscribe({
      next: (rows) => {
        this.pending.set(rows);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorText(err));
        this.loading.set(false);
      }
    });
  }

  decide(request: TimeRequest, status: 'APPROVED' | 'REJECTED'): void {
    const action$ = status === 'APPROVED'
      ? this.api.approveRequest(request.id)
      : this.api.rejectRequest(request.id);
    action$.subscribe({
      next: () => {
        this.notice.set(status === 'APPROVED' ? 'Request approved.' : 'Request rejected.');
        this.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  addPerson(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const value = this.form.getRawValue();
    const body: { firstName: string; lastName: string; managerId?: number } = {
      firstName: value.firstName.trim(),
      lastName: value.lastName.trim(),
    };
    if (value.managerId) {
      body.managerId = Number(value.managerId);
    }
    this.api.createUser(body).subscribe({
      next: () => {
        this.notice.set('Person added.');
        this.form.reset({ firstName: '', lastName: '', managerId: '' });
        this.session.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  removePerson(id: number): void {
    this.api.deleteUser(id).subscribe({
      next: () => {
        this.confirmId.set(null);
        this.notice.set('Person removed.');
        this.session.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }
}
