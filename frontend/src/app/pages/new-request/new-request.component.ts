import { Component, effect, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { Balance } from '../../models';
import { errorText, inclusiveDays, todayIso } from '../../format';

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
  readonly saving = signal(false);
  readonly error = signal('');
  readonly notice = signal('');

  readonly form = this.fb.nonNullable.group({
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    balanceId: ['', Validators.required],
    explanation: ['', [Validators.required, Validators.minLength(4)]],
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
      if (!id) {
        this.balances.set([]);
        return;
      }
      this.api.balances(id).subscribe({
        next: (rows) => {
          this.balances.set(rows);
          if (!this.form.controls.balanceId.value && rows[0]) {
            this.form.controls.balanceId.setValue(String(rows[0].id));
          }
        },
        error: (err) => this.error.set(errorText(err))
      });
    }, { allowSignalWrites: true });
  }

  selectedBalance(): Balance | undefined {
    const id = Number(this.form.controls.balanceId.value);
    return this.balances().find((balance) => balance.id === id);
  }

  days(): number {
    const value = this.form.getRawValue();
    return inclusiveDays(value.startDate, value.endDate);
  }

  overBalance(): boolean {
    const balance = this.selectedBalance();
    const days = this.days();
    return !!balance && days > Number(balance.daysLeft);
  }

  submit(): void {
    this.form.markAllAsTouched();
    this.error.set('');
    this.notice.set('');
    const employeeId = this.session.current()?.id;
    if (!employeeId || this.form.invalid) return;

    const value = this.form.getRawValue();
    const today = todayIso();
    this.saving.set(true);
    this.api.createRequest({
      employee: { id: employeeId },
      balance: { id: Number(value.balanceId) },
      status: 'PENDING',
      startDate: value.startDate,
      endDate: value.endDate,
      explanation: value.explanation.trim(),
      creationDate: today,
      modifiedDate: today,
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.notice.set('Request sent. It stays pending until a manager reviews it.');
        this.form.patchValue({ explanation: '', startDate: '', endDate: '' });
        this.form.markAsUntouched();
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(errorText(err));
      }
    });
  }
}
