import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { SettingsStore } from '../state/settings.store';

/** Adds the X-API-Key header to every API request. */
export const apiKeyInterceptor: HttpInterceptorFn = (req, next) => {
  const apiKey = inject(SettingsStore).apiKey();
  if (apiKey && req.url.startsWith('/api/')) {
    return next(req.clone({ setHeaders: { 'X-API-Key': apiKey } }));
  }
  return next(req);
};
