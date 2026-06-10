import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { ApiClient } from '../../shared/api/api-client.service';
import { RepositoryDetail, ScanSummary } from '../../shared/api/api.types';
import { RemoteData } from '../../shared/state/remote-data';
import { FindingsTable } from '../../shared/ui/findings-table';
import { LoadingError } from '../../shared/ui/loading-error';
import { ScoreBadge } from '../../shared/ui/score-badge';

@Component({
  selector: 'app-repository-detail-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, RouterLink, MatCardModule, FindingsTable, LoadingError, ScoreBadge],
  templateUrl: './repository-detail-page.html',
  styleUrl: './repository-detail-page.scss',
})
export class RepositoryDetailPage {
  private readonly api = inject(ApiClient);

  /** Route parameter, bound via withComponentInputBinding(). */
  readonly id = input.required<string>();

  protected readonly repository = new RemoteData<RepositoryDetail>(() => this.api.getRepository(this.id()));
  protected readonly scans = new RemoteData<ScanSummary[]>(() => this.api.listScans(this.id()));

  constructor() {
    queueMicrotask(() => {
      this.repository.load();
      this.scans.load();
    });
  }

  protected reload(): void {
    this.repository.load();
  }
}
