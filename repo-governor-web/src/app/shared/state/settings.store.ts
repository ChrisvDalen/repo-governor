import { Injectable, signal } from '@angular/core';

const API_KEY_STORAGE_KEY = 'repo-governor.api-key';

/**
 * Holds the API key used for all server calls. Persisted in localStorage so a
 * page reload keeps the session.
 */
@Injectable({ providedIn: 'root' })
export class SettingsStore {
  private readonly apiKeySignal = signal<string | null>(localStorage.getItem(API_KEY_STORAGE_KEY));

  readonly apiKey = this.apiKeySignal.asReadonly();

  hasApiKey(): boolean {
    return !!this.apiKeySignal();
  }

  setApiKey(apiKey: string): void {
    localStorage.setItem(API_KEY_STORAGE_KEY, apiKey);
    this.apiKeySignal.set(apiKey);
  }

  clearApiKey(): void {
    localStorage.removeItem(API_KEY_STORAGE_KEY);
    this.apiKeySignal.set(null);
  }
}
