import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { ApiClient } from '../../shared/api/api-client.service';
import { Organization } from '../../shared/api/api.types';
import { RemoteData } from '../../shared/state/remote-data';
import { LoadingError } from '../../shared/ui/loading-error';

@Component({
  selector: 'app-organizations-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, MatCardModule, LoadingError],
  template: `
    <h1>Organizations</h1>
    <app-loading-error [loading]="organizations.loading()" [error]="organizations.error()" />
    @if (organizations.data(); as orgs) {
      @for (org of orgs; track org.id) {
        <mat-card class="org-card">
          <mat-card-content>
            <strong>{{ org.name }}</strong>
            <span class="since">since {{ org.createdAt | date: 'mediumDate' }}</span>
          </mat-card-content>
        </mat-card>
      }
    }
  `,
  styles: `
    .org-card {
      max-width: 480px;
      margin-bottom: 0.75rem;
    }
    .since {
      margin-left: 0.75rem;
      color: rgba(0, 0, 0, 0.5);
      font-size: 0.85rem;
    }
  `,
})
export class OrganizationsPage {
  private readonly api = inject(ApiClient);

  protected readonly organizations = new RemoteData<Organization[]>(() => this.api.listOrganizations());

  constructor() {
    this.organizations.load();
  }
}
