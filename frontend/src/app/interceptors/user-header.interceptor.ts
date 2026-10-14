import { HttpInterceptorFn } from '@angular/common/http';

export const userHeaderInterceptor: HttpInterceptorFn = (req, next) => {
  const userId = localStorage.getItem('tom-user-id') || '2';
  req = req.clone({
    setHeaders: {
      'X-User-Id': userId
    }
  });
  return next(req);
};
