import { Injectable, computed, inject, signal } from '@angular/core';
import { ApiService } from './api.service';
import { AppUser, Role } from '../models';

const STORAGE_KEY = 'tom-user-id';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly api = inject(ApiService);

  readonly users = signal<AppUser[]>([]);
  readonly current = signal<AppUser | null>(null);
  readonly ready = signal(false);
  readonly offline = signal(false);

  readonly role = computed<Role | null>(() => this.current()?.role ?? null);
  readonly isAdmin = computed(() => this.role() === 'ADMIN');
  readonly isManager = computed(() => this.role() === 'MANAGER' || this.role() === 'ADMIN');
  readonly isEmployee = computed(() => this.role() === 'EMPLOYEE');
  readonly roleLabel = computed(() => {
    const r = this.role();
    if (r === 'ADMIN') return 'Admin';
    if (r === 'MANAGER') return 'Manager';
    return 'Employee';
  });

  readonly team = computed(() => {
    const id = this.current()?.id;
    return this.users().filter((user) => user.manager?.id === id);
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.api.users().subscribe({
      next: (users) => {
        const list = users ?? [];
        this.offline.set(false);
        this.users.set(list);
        const saved = Number(localStorage.getItem(STORAGE_KEY) || 2);
        const next = list.find((user) => user.id === saved) ?? list.find((user) => user.id === 2) ?? list[0] ?? null;
        this.current.set(next);
        if (next) {
          localStorage.setItem(STORAGE_KEY, String(next.id));
        }
        this.ready.set(true);
      },
      error: () => {
        this.offline.set(true);
        this.ready.set(true);
      }
    });
  }

  select(id: number): void {
    const found = this.users().find((user) => user.id === id);
    if (!found) {
      return;
    }
    this.current.set(found);
    localStorage.setItem(STORAGE_KEY, String(id));
  }
}
