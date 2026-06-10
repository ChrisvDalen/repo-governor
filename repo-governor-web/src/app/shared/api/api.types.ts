// Types mirroring repo-governor-contracts/openapi.yaml.

export type Severity = 'BLOCKER' | 'MAJOR' | 'MINOR' | 'INFO';

export type Category =
  | 'REPO_HEALTH'
  | 'JAVA'
  | 'SPRING_BOOT'
  | 'ANGULAR'
  | 'API_CONTRACT'
  | 'CI_CD'
  | 'SECURITY'
  | 'AI_READINESS'
  | 'DOCUMENTATION';

export type FindingStatus = 'OPEN' | 'ACCEPTED_RISK' | 'RESOLVED';

export interface Organization {
  id: string;
  name: string;
  createdAt: string;
}

export interface CategoryScore {
  category: Category;
  score: number;
}

export interface Finding {
  id: string;
  ruleId: string;
  title: string;
  description: string | null;
  severity: Severity;
  category: Category;
  filePath: string | null;
  lineNumber: number | null;
  recommendation: string | null;
  status: FindingStatus;
  createdAt: string;
}

export interface RepositorySummary {
  id: string;
  name: string;
  teamName: string | null;
  latestScore: number | null;
  blockerCount: number;
  majorCount: number;
  lastScannedAt: string | null;
  detectedTechnologies: string[];
}

export interface ScanSummary {
  id: string;
  repositoryId: string;
  branch: string | null;
  commitHash: string | null;
  scannedAt: string;
  toolVersion: string;
  overallScore: number;
  blockerCount: number;
  majorCount: number;
}

export interface ScanDetail extends ScanSummary {
  categoryScores: CategoryScore[];
  findings: Finding[];
  detectedTechnologies: string[];
  metadata: Record<string, string>;
}

export interface RepositoryDetail extends RepositorySummary {
  latestScan: ScanDetail | null;
}

export interface ScanDiff {
  scanId: string;
  previousScanId: string | null;
  scoreDelta: number;
  newFindings: Finding[];
  resolvedFindings: Finding[];
}

export interface RepositoryTrend {
  repositoryId: string;
  name: string;
  currentScore: number;
  previousScore: number;
}

export interface DashboardSummary {
  organizationId: string;
  overallScore: number;
  repositoryCount: number;
  blockerFindings: number;
  majorFindings: number;
  worstRepositories: RepositorySummary[];
  improvingRepositories: RepositoryTrend[];
  degradingRepositories: RepositoryTrend[];
  latestScans: ScanSummary[];
}

export interface RuleWithConfiguration {
  id: string;
  ruleKey: string;
  title: string;
  description: string | null;
  category: Category;
  severity: Severity;
  enabled: boolean;
  threshold: number | null;
}
