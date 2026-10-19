import { Component, effect, inject, signal } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { TimeRequest } from '../../models';
import { errorText, fullName, prettyDate, statusLabel, workingDays } from '../../format';

@Component({
  selector: 'app-approvals',
  standalone: true,
  imports: [],
  templateUrl: './approvals.component.html',
  styleUrl: './approvals.component.scss'
})
export class ApprovalsComponent {
  private readonly api = inject(ApiService);
  readonly session = inject(SessionService);

  readonly pending = signal<TimeRequest[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly comments = signal<Record<number, string>>({});

  readonly fullName = fullName;
  readonly prettyDate = prettyDate;
  readonly statusLabel = statusLabel;
  readonly workingDays = workingDays;

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
    const includeUnassigned = this.session.isAdmin();
    this.api.pending(id, includeUnassigned).subscribe({
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

  setComment(requestId: number, value: string): void {
    this.comments.update((prev) => ({ ...prev, [requestId]: value }));
  }

  decide(request: TimeRequest, status: 'APPROVED' | 'REJECTED'): void {
    this.error.set('');
    this.notice.set('');
    const comment = (this.comments()[request.id] ?? '').trim() || undefined;
    const action$ = status === 'APPROVED'
      ? this.api.approveRequest(request.id, comment)
      : this.api.rejectRequest(request.id, comment);
    action$.subscribe({
      next: () => {
        this.notice.set(status === 'APPROVED' ? 'Request approved.' : 'Request rejected.');
        this.comments.update((prev) => {
          const next = { ...prev };
          delete next[request.id];
          return next;
        });
        this.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }
}
