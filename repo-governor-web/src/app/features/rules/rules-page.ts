import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { ApiClient } from '../../shared/api/api-client.service';
import { RuleWithConfiguration } from '../../shared/api/api.types';
import { RemoteData } from '../../shared/state/remote-data';
import { LoadingError } from '../../shared/ui/loading-error';
import { SeverityChip } from '../../shared/ui/severity-chip';

@Component({
  selector: 'app-rules-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatTableModule, MatSlideToggleModule, LoadingError, SeverityChip],
  templateUrl: './rules-page.html',
  styleUrl: './rules-page.scss',
})
export class RulesPage {
  private readonly api = inject(ApiClient);

  protected readonly columns = ['rule', 'category', 'severity', 'threshold', 'enabled'];
  protected readonly rules = new RemoteData<RuleWithConfiguration[]>(() => this.api.listRules());

  constructor() {
    this.rules.load();
  }

  protected async toggle(rule: RuleWithConfiguration, enabled: boolean): Promise<void> {
    await this.api.updateRuleConfiguration(rule.id, { enabled });
    this.rules.load();
  }
}
