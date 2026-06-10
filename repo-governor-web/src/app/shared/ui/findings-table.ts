import { ChangeDetectionStrategy, Component, inject, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiClient } from '../api/api-client.service';
import { Finding, FindingStatus } from '../api/api.types';
import { SeverityChip } from './severity-chip';

/**
 * Findings table with optional triage actions (accept risk / resolve).
 */
@Component({
  selector: 'app-findings-table',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatTableModule, MatButtonModule, MatTooltipModule, SeverityChip],
  templateUrl: './findings-table.html',
  styleUrl: './findings-table.scss',
})
export class FindingsTable {
  private readonly api = inject(ApiClient);

  readonly findings = input.required<Finding[]>();
  readonly triageEnabled = input(false);
  readonly statusChanged = output<Finding>();

  protected get columns(): string[] {
    return this.triageEnabled()
      ? ['severity', 'rule', 'category', 'file', 'status', 'actions']
      : ['severity', 'rule', 'category', 'file', 'status'];
  }

  protected async setStatus(finding: Finding, status: FindingStatus): Promise<void> {
    const updated = await this.api.updateFindingStatus(finding.id, status);
    this.statusChanged.emit(updated);
  }
}
