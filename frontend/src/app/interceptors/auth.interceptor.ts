import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AUTH_TOKEN_KEY, SessionService, isJwtExpired } from '../services/session.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const isLoginRequest = req.url.includes('/api/auth/login');
  const token = localStorage.getItem(AUTH_TOKEN_KEY);

  if (token && !isLoginRequest) {
    if (isJwtExpired(token)) {
      const session = inject(SessionService);
      session.logout('Your session has expired. Please sign in again.');
      return throwError(() => new HttpErrorResponse({ status: 401, statusText: 'Token Expired', url: req.url }));
    }

    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse && err.status === 401 && !isLoginRequest) {
        const session = inject(SessionService);
        session.logout('Your session has expired. Please sign in again.');
      }
      return throwError(() => err);
    })
  );
};
