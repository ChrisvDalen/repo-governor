import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Severity } from '../api/api.types';

@Component({
  selector: 'app-severity-chip',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="chip" [class]="'chip-' + severity().toLowerCase()">{{ severity() }}</span>`,
  styles: `
    .chip {
      padding: 0.1rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 600;
      color: #fff;
    }
    .chip-blocker { background: #b71c1c; }
    .chip-major { background: #e65100; }
    .chip-minor { background: #f9a825; color: #333; }
    .chip-info { background: #546e7a; }
  `,
})
export class SeverityChip {
  readonly severity = input.required<Severity>();
}
