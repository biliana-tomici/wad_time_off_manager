import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { SessionService } from '../../services/session.service';
import { ApiService } from '../../services/api.service';
import { Balance, TimeRequest } from '../../models';
import { errorText, fullName, inclusiveDays, prettyDate, statusLabel } from '../../format';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  readonly session = inject(SessionService);
  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);

  readonly balances = signal<Balance[]>([]);
  readonly requests = signal<TimeRequest[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly showAllowance = signal(false);
  readonly prettyDate = prettyDate;
  readonly statusLabel = statusLabel;
  readonly inclusiveDays = inclusiveDays;
  readonly fullName = fullName;

  readonly daysLeft = computed(() => this.balances().reduce((sum, balance) => sum + Number(balance.daysLeft || 0), 0));
  readonly pendingCount = computed(() => this.requests().filter((request) => request.status === 'PENDING' || request.status === 'EDITED').length);
  readonly approvedCount = computed(() => this.requests().filter((request) => request.status === 'APPROVED').length);
  readonly recent = computed(() => this.requests().slice(0, 4));

  readonly allowanceForm = this.fb.nonNullable.group({
    name: ['Vacation', Validators.required],
    daysLeft: [10, [Validators.required, Validators.min(0)]],
    maxDays: [20, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    effect(() => {
      const id = this.session.current()?.id;
      if (!id) {
        this.balances.set([]);
        this.requests.set([]);
        return;
      }
      this.load(id);
    }, { allowSignalWrites: true });
  }

  percent(balance: Balance): number {
    if (!balance.maxDays) return 0;
    return Math.max(0, Math.min(100, (Number(balance.daysLeft) / Number(balance.maxDays)) * 100));
  }

  load(id = this.session.current()?.id): void {
    if (!id) return;
    this.loading.set(true);
    this.error.set('');
    forkJoin({
      balances: this.api.balances(id),
      requests: this.api.requests(id),
    }).subscribe({
      next: ({ balances, requests }) => {
        this.balances.set(balances);
        this.requests.set(requests);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorText(err));
        this.loading.set(false);
      }
    });
  }

  addAllowance(): void {
    const employeeId = this.session.current()?.id;
    if (!employeeId || this.allowanceForm.invalid) {
      this.allowanceForm.markAllAsTouched();
      return;
    }
    const value = this.allowanceForm.getRawValue();
    this.api.createBalance({
      employeeId,
      name: value.name,
      daysLeft: Number(value.daysLeft),
      maxDays: Number(value.maxDays),
    }).subscribe({
      next: () => {
        this.notice.set('Allowance added.');
        this.showAllowance.set(false);
        this.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  removeBalance(balance: Balance): void {
    this.api.deleteBalance(balance.id).subscribe({
      next: () => this.load(),
      error: (err) => this.error.set(errorText(err))
    });
  }
}
