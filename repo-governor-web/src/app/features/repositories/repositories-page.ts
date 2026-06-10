import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { ApiClient } from '../../shared/api/api-client.service';
import { RepositorySummary } from '../../shared/api/api.types';
import { RemoteData } from '../../shared/state/remote-data';
import { LoadingError } from '../../shared/ui/loading-error';
import { ScoreBadge } from '../../shared/ui/score-badge';

@Component({
  selector: 'app-repositories-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, RouterLink, MatTableModule, MatChipsModule, LoadingError, ScoreBadge],
  templateUrl: './repositories-page.html',
  styleUrl: './repositories-page.scss',
})
export class RepositoriesPage {
  private readonly api = inject(ApiClient);

  protected readonly columns = ['name', 'team', 'score', 'blockers', 'majors', 'lastScanned', 'technologies'];
  protected readonly repositories = new RemoteData<RepositorySummary[]>(() => this.api.listRepositories());

  constructor() {
    this.repositories.load();
  }
}
