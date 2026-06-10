import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { SettingsStore } from './shared/state/settings.store';

@Component({
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly settings = inject(SettingsStore);

  protected readonly navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: 'insights' },
    { path: '/repositories', label: 'Repositories', icon: 'folder' },
    { path: '/rules', label: 'Rules', icon: 'rule' },
    { path: '/organizations', label: 'Organizations', icon: 'apartment' },
    { path: '/settings', label: 'Settings', icon: 'settings' },
  ];
}
