import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-score-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (score() !== null) {
      <span class="score" [class]="scoreClass()">{{ score() }}</span>
    } @else {
      <span class="score score-none">—</span>
    }
  `,
  styles: `
    .score {
      display: inline-block;
      min-width: 2.5rem;
      padding: 0.15rem 0.5rem;
      border-radius: 999px;
      text-align: center;
      font-weight: 600;
      color: #fff;
    }
    .score-good { background: #2e7d32; }
    .score-medium { background: #ef6c00; }
    .score-bad { background: #c62828; }
    .score-none { background: #9e9e9e; }
  `,
})
export class ScoreBadge {
  readonly score = input.required<number | null>();

  protected readonly scoreClass = computed(() => {
    const value = this.score();
    if (value === null) return 'score-none';
    if (value >= 80) return 'score-good';
    if (value >= 60) return 'score-medium';
    return 'score-bad';
  });
}
