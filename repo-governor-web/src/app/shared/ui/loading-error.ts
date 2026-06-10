import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loading-error',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatProgressSpinnerModule],
  template: `
    @if (loading()) {
      <div class="state"><mat-spinner diameter="36" /></div>
    } @else if (error()) {
      <div class="state error">{{ error() }}</div>
    }
  `,
  styles: `
    .state {
      display: flex;
      justify-content: center;
      padding: 2rem;
    }
    .error {
      color: #c62828;
      font-weight: 500;
    }
  `,
})
export class LoadingError {
  readonly loading = input.required<boolean>();
  readonly error = input.required<string | null>();
}
