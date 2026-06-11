import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { DatePipe, KeyValuePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { ApiClient } from '../../shared/api/api-client.service';
import { ScanDetail, ScanDiff } from '../../shared/api/api.types';
import { RemoteData } from '../../shared/state/remote-data';
import { FindingsTable } from '../../shared/ui/findings-table';
import { LoadingError } from '../../shared/ui/loading-error';
import { ScoreBadge } from '../../shared/ui/score-badge';

@Component({
  selector: 'app-scan-detail-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, KeyValuePipe, RouterLink, MatCardModule, FindingsTable, LoadingError, ScoreBadge],
  templateUrl: './scan-detail-page.html',
  styleUrl: './scan-detail-page.scss',
})
export class ScanDetailPage {
  private readonly api = inject(ApiClient);

  /** Route parameters, bound via withComponentInputBinding(). */
  readonly id = input.required<string>();
  readonly scanId = input.required<string>();

  protected readonly scan = new RemoteData<ScanDetail>(() => this.api.getScan(this.scanId()));
  protected readonly diff = new RemoteData<ScanDiff>(() => this.api.getScanDiff(this.scanId()));

  constructor() {
    queueMicrotask(() => {
      this.scan.load();
      this.diff.load();
    });
  }
}
