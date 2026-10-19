import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { toObservable } from '@angular/core/rxjs-interop';
import { filter, map, take } from 'rxjs';
import { SessionService, isJwtExpired } from '../services/session.service';

export const authGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);

  const check = () => {
    const token = session.token();
    if (!session.isAuthenticated() || !token) {
      return router.createUrlTree(['/login']);
    }
    if (isJwtExpired(token)) {
      session.logout('Your session has expired. Please sign in again.');
      return router.createUrlTree(['/login']);
    }
    return true;
  };

  if (session.ready()) {
    return check();
  }

  return toObservable(session.ready).pipe(
    filter(Boolean),
    take(1),
    map(() => check())
  );
};

export const guestGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);

  const check = () => {
    return session.isAuthenticated() ? router.createUrlTree(['/home']) : true;
  };

  if (session.ready()) {
    return check();
  }

  return toObservable(session.ready).pipe(
    filter(Boolean),
    take(1),
    map(() => check())
  );
};
