import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { ApiClient } from '../../shared/api/api-client.service';
import { DashboardSummary } from '../../shared/api/api.types';
import { RemoteData } from '../../shared/state/remote-data';
import { LoadingError } from '../../shared/ui/loading-error';
import { ScoreBadge } from '../../shared/ui/score-badge';

@Component({
  selector: 'app-dashboard-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, RouterLink, MatCardModule, MatTableModule, LoadingError, ScoreBadge],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.scss',
})
export class DashboardPage {
  private readonly api = inject(ApiClient);

  protected readonly dashboard = new RemoteData<DashboardSummary>(async () => {
    const organizations = await this.api.listOrganizations();
    if (organizations.length === 0) {
      throw new Error('No organizations found. Upload a scan first.');
    }
    return this.api.getDashboard(organizations[0].id);
  });

  constructor() {
    this.dashboard.load();
  }
}
