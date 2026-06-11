import { signal } from '@angular/core';

/**
 * Tiny signal-based loading/error/data wrapper used by all feature pages.
 */
export class RemoteData<T> {
  readonly data = signal<T | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  constructor(private readonly loader: () => Promise<T>) {}

  async load(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      this.data.set(await this.loader());
    } catch (e: unknown) {
      this.data.set(null);
      this.error.set(toErrorMessage(e));
    } finally {
      this.loading.set(false);
    }
  }
}

function toErrorMessage(e: unknown): string {
  const err = e as { status?: number; message?: string };
  if (err.status === 401) {
    return 'Unauthorized: configure a valid API key under Settings.';
  }
  if (err.status === 0) {
    return 'Server unreachable. Is repo-governor-server running on port 8080?';
  }
  return err.message ?? 'Unexpected error';
}
