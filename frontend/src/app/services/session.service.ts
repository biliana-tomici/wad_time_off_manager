import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { AppUser, AuthResponse, Role } from '../models';

export const AUTH_TOKEN_KEY = 'tom-auth-token';

export function getTokenExpiryMs(token: string): number | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = atob(base64);
    const payload = JSON.parse(json) as { exp?: number };
    return typeof payload.exp === 'number' ? payload.exp * 1000 : null;
  } catch {
    return null;
  }
}

export function isJwtExpired(token: string): boolean {
  const expMs = getTokenExpiryMs(token);
  if (expMs === null) return false;
  return Date.now() >= expMs;
}

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private expiryTimer: ReturnType<typeof setTimeout> | null = null;

  readonly token = signal<string | null>(null);
  readonly users = signal<AppUser[]>([]);
  readonly current = signal<AppUser | null>(null);
  readonly ready = signal(false);
  readonly offline = signal(false);
  readonly sessionNotice = signal('');

  readonly isAuthenticated = computed(() => Boolean(this.token() && this.current()));
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
    localStorage.removeItem('tom-user-id');
    this.initSession();
  }

  private initSession(): void {
    const savedToken = localStorage.getItem(AUTH_TOKEN_KEY);
    if (!savedToken) {
      this.ready.set(true);
      return;
    }

    if (isJwtExpired(savedToken)) {
      this.clearState('Your session has expired. Please sign in again.');
      this.ready.set(true);
      return;
    }

    this.token.set(savedToken);
    this.scheduleExpiryWatch(savedToken);

    this.api.me().subscribe({
      next: (me) => {
        this.offline.set(false);
        this.current.set(me);
        this.ready.set(true);
        this.loadUsersDirectory();
      },
      error: (err) => {
        if (err?.status === 0) {
          this.offline.set(true);
        } else {
          this.clearState('Your session has expired. Please sign in again.');
        }
        this.ready.set(true);
      }
    });
  }

  load(): void {
    if (!this.token()) {
      this.ready.set(true);
      return;
    }
    this.loadUsersDirectory();
  }

  private loadUsersDirectory(): void {
    this.api.users().subscribe({
      next: (users) => {
        const list = users ?? [];
        this.offline.set(false);
        this.users.set(list);
        const currentId = this.current()?.id;
        if (currentId) {
          const updatedMe = list.find((u) => u.id === currentId);
          if (updatedMe) {
            this.current.set(updatedMe);
          }
        }
      },
      error: (err) => {
        if (err?.status === 0) {
          this.offline.set(true);
        }
      }
    });
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.api.login(email, password).pipe(
      tap((res) => {
        localStorage.setItem(AUTH_TOKEN_KEY, res.token);
        this.token.set(res.token);
        this.current.set(res.user);
        this.sessionNotice.set('');
        this.offline.set(false);
        this.ready.set(true);
        this.scheduleExpiryWatch(res.token);
        this.loadUsersDirectory();
      })
    );
  }

  logout(notice = ''): void {
    this.clearState(notice);
    this.ready.set(true);
    void this.router.navigate(['/login']);
  }

  private clearState(notice = ''): void {
    if (this.expiryTimer) {
      clearTimeout(this.expiryTimer);
      this.expiryTimer = null;
    }
    localStorage.removeItem(AUTH_TOKEN_KEY);
    this.token.set(null);
    this.current.set(null);
    this.users.set([]);
    if (notice) {
      this.sessionNotice.set(notice);
    }
  }

  private scheduleExpiryWatch(token: string): void {
    if (this.expiryTimer) {
      clearTimeout(this.expiryTimer);
      this.expiryTimer = null;
    }
    const expMs = getTokenExpiryMs(token);
    if (expMs === null) return;
    const delay = Math.max(0, expMs - Date.now());
    this.expiryTimer = setTimeout(() => {
      this.logout('Your session has expired. Please sign in again.');
    }, delay);
  }
}
