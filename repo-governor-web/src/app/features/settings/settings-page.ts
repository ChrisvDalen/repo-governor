import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ApiClient } from '../../shared/api/api-client.service';
import { SettingsStore } from '../../shared/state/settings.store';

@Component({
  selector: 'app-settings-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule],
  templateUrl: './settings-page.html',
  styleUrl: './settings-page.scss',
})
export class SettingsPage {
  private readonly api = inject(ApiClient);
  private readonly router = inject(Router);
  protected readonly settings = inject(SettingsStore);

  protected apiKeyInput = this.settings.apiKey() ?? '';
  protected readonly message = signal<string | null>(null);
  protected readonly checking = signal(false);

  protected async save(): Promise<void> {
    this.settings.setApiKey(this.apiKeyInput.trim());
    this.checking.set(true);
    this.message.set(null);
    try {
      await this.api.listOrganizations();
      this.message.set('API key works — redirecting to the dashboard…');
      await this.router.navigate(['/dashboard']);
    } catch {
      this.message.set('The server rejected this API key. For local development use "dev-api-key".');
    } finally {
      this.checking.set(false);
    }
  }

  protected clear(): void {
    this.settings.clearApiKey();
    this.apiKeyInput = '';
    this.message.set('API key cleared.');
  }
}
