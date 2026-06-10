import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard-page').then((m) => m.DashboardPage),
  },
  {
    path: 'organizations',
    loadComponent: () =>
      import('./features/organizations/organizations-page').then((m) => m.OrganizationsPage),
  },
  {
    path: 'repositories',
    loadComponent: () =>
      import('./features/repositories/repositories-page').then((m) => m.RepositoriesPage),
  },
  {
    path: 'repositories/:id',
    loadComponent: () =>
      import('./features/repositories/repository-detail-page').then((m) => m.RepositoryDetailPage),
  },
  {
    path: 'repositories/:id/scans/:scanId',
    loadComponent: () => import('./features/scans/scan-detail-page').then((m) => m.ScanDetailPage),
  },
  {
    path: 'rules',
    loadComponent: () => import('./features/rules/rules-page').then((m) => m.RulesPage),
  },
  {
    path: 'settings',
    loadComponent: () => import('./features/settings/settings-page').then((m) => m.SettingsPage),
  },
  { path: '**', redirectTo: 'dashboard' },
];
