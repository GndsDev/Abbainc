import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { API_BASE_URL } from '../config/api';
import { AuthService } from './auth';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const authorizationHeader = authService.authorizationHeader;

  const request = authorizationHeader && req.url.startsWith(API_BASE_URL)
    ? req.clone({ setHeaders: { Authorization: authorizationHeader } })
    : req;

  return next(request).pipe(
    catchError((erro) => {
      if (erro.status === 401) {
        authService.logout();
      }

      return throwError(() => erro);
    })
  );
};
