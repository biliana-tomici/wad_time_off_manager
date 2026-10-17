import { Component, effect, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { Balance, TimeRequest } from '../../models';
import { errorText, workingDays } from '../../format';

@Component({
  selector: 'app-new-request',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './new-request.component.html',
  styleUrl: './new-request.component.scss'
})
export class NewRequestComponent {
  private readonly api = inject(ApiService);
  readonly session = inject(SessionService);
  private readonly fb = inject(FormBuilder);

  readonly balances = signal<Balance[]>([]);
  readonly requests = signal<TimeRequest[]>([]);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly notice = signal('');

  readonly form = this.fb.nonNullable.group({
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    balanceId: ['', Validators.required],
    explanation: ['', [Validators.maxLength(255)]],
  }, {
    validators: (group) => {
      const start = group.get('startDate')?.value;
      const end = group.get('endDate')?.value;
      return start && end && end < start ? { range: true } : null;
    }
  });

  constructor() {
    effect(() => {
      const id = this.session.current()?.id;
      this.notice.set('');
      this.error.set('');
      if (!id) {
        this.balances.set([]);
        this.requests.set([]);
        return;
      }
      this.load(id);
    }, { allowSignalWrites: true });
  }

  load(id = this.session.current()?.id): void {
    if (!id) return;
    forkJoin({
      balances: this.api.balances(id),
      requests: this.api.requests(id),
    }).subscribe({
      next: ({ balances, requests }) => {
        this.balances.set(balances);
        this.requests.set(requests);
        if (!this.form.controls.balanceId.value && balances[0]) {
          this.form.controls.balanceId.setValue(String(balances[0].id));
        }
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  selectedBalance(): Balance | undefined {
    const id = Number(this.form.controls.balanceId.value);
    return this.balances().find((balance) => balance.id === id);
  }

  reservedDays(balance: Balance): number {
    return this.requests()
      .filter((r) => r.balanceId === balance.id && (r.status === 'PENDING' || r.status === 'EDITED'))
      .reduce((sum, r) => sum + workingDays(r.startDate, r.endDate), 0);
  }

  availableDays(balance: Balance): number {
    return Math.max(0, Number(balance.daysLeft) - this.reservedDays(balance));
  }

  days(): number {
    const value = this.form.getRawValue();
    return workingDays(value.startDate, value.endDate);
  }

  overBalance(): boolean {
    const balance = this.selectedBalance();
    const days = this.days();
    return !!balance && days > this.availableDays(balance);
  }

  submit(): void {
    this.form.markAllAsTouched();
    this.error.set('');
    this.notice.set('');
    const employeeId = this.session.current()?.id;
    if (!employeeId || this.form.invalid) return;

    const value = this.form.getRawValue();
    this.saving.set(true);
    this.api.createRequest({
      employeeId,
      balanceId: Number(value.balanceId),
      startDate: value.startDate,
      endDate: value.endDate,
      explanation: value.explanation.trim(),
    }).subscribe({
      next: (created) => {
        this.saving.set(false);
        const count = created.workingDays ?? this.days();
        this.notice.set(`Request sent (${count} working ${count === 1 ? 'day' : 'days'}). It stays pending until a manager reviews it.`);
        this.form.patchValue({ explanation: '', startDate: '', endDate: '' });
        this.form.markAsUntouched();
        this.load(employeeId);
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(errorText(err));
      }
    });
  }
}
