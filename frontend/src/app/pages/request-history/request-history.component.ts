import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { Balance, Status, TimeRequest } from '../../models';
import { errorText, inclusiveDays, prettyDate, statusLabel } from '../../format';

@Component({
  selector: 'app-request-history',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './request-history.component.html',
  styleUrl: './request-history.component.scss'
})
export class RequestHistoryComponent {
  private readonly api = inject(ApiService);
  readonly session = inject(SessionService);
  private readonly fb = inject(FormBuilder);

  readonly requests = signal<TimeRequest[]>([]);
  readonly balances = signal<Balance[]>([]);
  readonly filter = signal<'ALL' | Status>('ALL');
  readonly editingId = signal<number | null>(null);
  readonly confirmId = signal<number | null>(null);
  readonly error = signal('');
  readonly notice = signal('');
  readonly loading = signal(false);
  readonly prettyDate = prettyDate;
  readonly statusLabel = statusLabel;
  readonly inclusiveDays = inclusiveDays;
  readonly filters: Array<'ALL' | Status> = ['ALL', 'PENDING', 'APPROVED', 'REJECTED', 'EDITED', 'CANCELLED'];

  readonly visible = computed(() => {
    const selected = this.filter();
    const rows = this.requests();
    return selected === 'ALL' ? rows : rows.filter((request) => request.status === selected);
  });

  readonly editForm = this.fb.nonNullable.group({
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    balanceId: ['', Validators.required],
    explanation: ['', Validators.required],
  });

  constructor() {
    effect(() => {
      const id = this.session.current()?.id;
      this.editingId.set(null);
      if (!id) {
        this.requests.set([]);
        return;
      }
      this.load(id);
    }, { allowSignalWrites: true });
  }

  load(id = this.session.current()?.id): void {
    if (!id) return;
    this.loading.set(true);
    this.error.set('');
    this.api.requests(id).subscribe({
      next: (rows) => {
        this.requests.set(rows);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorText(err));
        this.loading.set(false);
      }
    });
    this.api.balances(id).subscribe({
      next: (rows) => this.balances.set(rows),
      error: () => this.balances.set([])
    });
  }

  canEdit(request: TimeRequest): boolean {
    return request.status === 'PENDING' || request.status === 'EDITED';
  }

  startEdit(request: TimeRequest): void {
    this.editingId.set(request.id);
    this.editForm.setValue({
      startDate: request.startDate,
      endDate: request.endDate,
      balanceId: String(request.balance?.id ?? ''),
      explanation: request.explanation ?? '',
    });
  }

  saveEdit(request: TimeRequest): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }
    const value = this.editForm.getRawValue();
    this.api.updateRequest(request.id, {
      startDate: value.startDate,
      endDate: value.endDate,
      explanation: value.explanation,
      balance: { id: Number(value.balanceId) },
    }).subscribe({
      next: () => {
        this.notice.set('Request updated. Status is now Edited.');
        this.editingId.set(null);
        this.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  cancel(request: TimeRequest): void {
    this.api.setStatus(request.id, 'CANCELLED').subscribe({
      next: () => {
        this.notice.set('Request cancelled.');
        this.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  remove(request: TimeRequest): void {
    this.api.deleteRequest(request.id).subscribe({
      next: () => {
        this.confirmId.set(null);
        this.notice.set('Request deleted.');
        this.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }
}
