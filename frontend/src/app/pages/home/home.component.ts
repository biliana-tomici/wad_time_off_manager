import { Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { SessionService } from '../../services/session.service';
import { ApiService } from '../../services/api.service';
import { Balance, TimeRequest } from '../../models';
import { errorText, fullName, prettyDate, statusLabel, workingDays } from '../../format';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  readonly session = inject(SessionService);
  private readonly api = inject(ApiService);

  readonly balances = signal<Balance[]>([]);
  readonly requests = signal<TimeRequest[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly prettyDate = prettyDate;
  readonly statusLabel = statusLabel;
  readonly workingDays = workingDays;
  readonly fullName = fullName;

  readonly daysLeft = computed(() => this.balances().reduce((sum, balance) => sum + Number(balance.daysLeft || 0), 0));
  readonly pendingCount = computed(() => this.requests().filter((request) => request.status === 'PENDING' || request.status === 'EDITED').length);
  readonly approvedCount = computed(() => this.requests().filter((request) => request.status === 'APPROVED').length);
  readonly recent = computed(() => this.requests().slice(0, 4));

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

  reservedDays(balance: Balance): number {
    return this.requests()
      .filter((r) => r.balanceId === balance.id && (r.status === 'PENDING' || r.status === 'EDITED'))
      .reduce((sum, r) => sum + workingDays(r.startDate, r.endDate), 0);
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
}
