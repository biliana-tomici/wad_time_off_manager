import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { toObservable } from '@angular/core/rxjs-interop';
import { filter, map, take } from 'rxjs';
import { SessionService } from '../services/session.service';
import { Role } from '../models';

export const roleGuard = (allowedRoles: Role[]): CanActivateFn => {
  return () => {
    const session = inject(SessionService);
    const router = inject(Router);

    const check = () => {
      const role = session.role();
      return role && allowedRoles.includes(role) ? true : router.createUrlTree(['/home']);
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
};
