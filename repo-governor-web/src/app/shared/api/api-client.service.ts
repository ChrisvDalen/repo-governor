import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import {
  Category,
  DashboardSummary,
  Finding,
  FindingStatus,
  Organization,
  RepositoryDetail,
  RepositorySummary,
  RuleWithConfiguration,
  ScanDetail,
  ScanDiff,
  ScanSummary,
  Severity,
} from './api.types';

/**
 * The single place that talks to the Repo Governor REST API
 * (repo-governor-contracts/openapi.yaml). Components never use HttpClient
 * directly.
 */
@Injectable({ providedIn: 'root' })
export class ApiClient {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1';

  listOrganizations(): Promise<Organization[]> {
    return firstValueFrom(this.http.get<Organization[]>(`${this.base}/organizations`));
  }

  getDashboard(organizationId: string): Promise<DashboardSummary> {
    return firstValueFrom(
      this.http.get<DashboardSummary>(`${this.base}/organizations/${organizationId}/dashboard`),
    );
  }

  listRepositories(): Promise<RepositorySummary[]> {
    return firstValueFrom(this.http.get<RepositorySummary[]>(`${this.base}/repositories`));
  }

  getRepository(repositoryId: string): Promise<RepositoryDetail> {
    return firstValueFrom(
      this.http.get<RepositoryDetail>(`${this.base}/repositories/${repositoryId}`),
    );
  }

  listScans(repositoryId: string): Promise<ScanSummary[]> {
    return firstValueFrom(
      this.http.get<ScanSummary[]>(`${this.base}/repositories/${repositoryId}/scans`),
    );
  }

  listFindings(
    repositoryId: string,
    filters: { severity?: Severity; category?: Category; status?: FindingStatus } = {},
  ): Promise<Finding[]> {
    let params = new HttpParams();
    if (filters.severity) params = params.set('severity', filters.severity);
    if (filters.category) params = params.set('category', filters.category);
    if (filters.status) params = params.set('status', filters.status);
    return firstValueFrom(
      this.http.get<Finding[]>(`${this.base}/repositories/${repositoryId}/findings`, { params }),
    );
  }

  getScan(scanId: string): Promise<ScanDetail> {
    return firstValueFrom(this.http.get<ScanDetail>(`${this.base}/scans/${scanId}`));
  }

  getScanDiff(scanId: string): Promise<ScanDiff> {
    return firstValueFrom(this.http.get<ScanDiff>(`${this.base}/scans/${scanId}/diff`));
  }

  updateFindingStatus(findingId: string, status: FindingStatus): Promise<Finding> {
    return firstValueFrom(
      this.http.patch<Finding>(`${this.base}/findings/${findingId}/status`, { status }),
    );
  }

  listRules(): Promise<RuleWithConfiguration[]> {
    return firstValueFrom(this.http.get<RuleWithConfiguration[]>(`${this.base}/rules`));
  }

  updateRuleConfiguration(
    ruleId: string,
    configuration: { enabled?: boolean; threshold?: number | null },
  ): Promise<RuleWithConfiguration> {
    return firstValueFrom(
      this.http.patch<RuleWithConfiguration>(
        `${this.base}/rules/${ruleId}/configuration`,
        configuration,
      ),
    );
  }
}
