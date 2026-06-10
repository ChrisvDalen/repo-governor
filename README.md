# Repo Governor

Repo Governor is a developer governance platform that scans repositories and
scores them on engineering maturity. It helps developers, tech leads and
engineering managers keep repositories healthy, secure, maintainable and
consistent.

It consists of two parts:

1. **A CLI tool** — runs locally or in CI/CD, scans a repository, generates
   JSON/Markdown reports and uploads results to the dashboard.
2. **A SaaS dashboard** — shows repo health per organization/team/repository,
   trends over time, findings, scores and concrete improvement actions.

```
CLI scan ──▶ repo-governor-report.json ──▶ POST /api/v1/scan-reports ──▶ Angular dashboard
```

## Monorepo layout

| Module | Stack | Purpose |
|---|---|---|
| [`repo-governor-core`](repo-governor-core) | Plain Java (no Spring) | Domain model, 24 scan rules, scoring, JSON/Markdown reporting |
| [`repo-governor-cli`](repo-governor-cli) | Picocli | `init`, `scan`, `report`, `upload`, `rules list` |
| [`repo-governor-server`](repo-governor-server) | Spring Boot 4, PostgreSQL, Flyway | REST API, API-key auth, scan storage, diffing, dashboard aggregation |
| [`repo-governor-web`](repo-governor-web) | Angular 21, Material, signals | Dashboard UI |
| [`repo-governor-contracts`](repo-governor-contracts) | OpenAPI 3 | Contract-first API definition (served at `/api/docs/openapi.yaml`) |

> **Note on Java version:** the product targets Java 25; this build is pinned to
> Java 21 (the newest LTS in the development environment). Bump the toolchain in
> the root `build.gradle` when JDK 25 is available.

## Prerequisites (installation)

- JDK 21+
- Node.js 22+ and npm
- Docker (for PostgreSQL and the Testcontainers integration tests)

## Running the vertical slice locally

### 1. Start the backend

```bash
docker compose up -d postgres
./gradlew :repo-governor-server:bootRun
```

On startup the server seeds a development organization `demo-org` with the API
key `dev-api-key`.

### 2. Start the frontend

```bash
cd repo-governor-web
npm install
npm start          # http://localhost:4200, /api proxied to :8080
```

Open http://localhost:4200/settings and enter the API key `dev-api-key`.

### 3. Build the CLI

```bash
./gradlew :repo-governor-cli:installDist
alias repo-governor=$PWD/repo-governor-cli/build/install/repo-governor/bin/repo-governor
```

(or use the self-contained jar: `java -jar repo-governor-cli/build/libs/repo-governor-cli-0.1.0-all.jar`)

### 4. Scan a repository and upload the report

```bash
repo-governor init .                 # optional: generates repo-governor.yml
repo-governor scan ../some-demo-repo --upload --server http://localhost:8080 --api-key dev-api-key
```

This writes `repo-governor-report.json` and `repo-governor-report.md` into the
scanned repository and uploads the report. The scan exits non-zero when the
configured thresholds are violated (score below `thresholds.overall`, or any
BLOCKER finding when `blockerAllowed: false`), which makes it CI-friendly.

### 5. Open the dashboard

http://localhost:4200 — the repository appears with its score, category
breakdown, findings (with triage: accept risk / resolve), scan history and a
diff against the previous scan.

### Everything in Docker

```bash
docker compose up --build      # dashboard on http://localhost:8081
```

## CLI commands

```bash
repo-governor init [path]          # generate repo-governor.yml
repo-governor scan [path]          # scan and write JSON + Markdown reports
repo-governor scan . --upload --server http://localhost:8080 --api-key dev-api-key
repo-governor report [path]        # print the last report (--format json|markdown)
repo-governor upload [path] --api-key dev-api-key [--server URL] [--file report.json]
repo-governor rules list           # list all available rules
```

## Configuration: `repo-governor.yml`

```yaml
project:
  name: example-service
  type: auto
  organization: demo-org
  team: platform
server:
  url: http://localhost:8080
rules:
  enabled: []          # empty = all rules; otherwise an allowlist of rule ids
  disabled: []
thresholds:
  overall: 70          # minimum overall score
  blockerAllowed: false
```

## Rule categories & scoring

Rules cover: `REPO_HEALTH`, `JAVA`, `SPRING_BOOT`, `ANGULAR`, `API_CONTRACT`,
`CI_CD`, `SECURITY`, `AI_READINESS`, `DOCUMENTATION`. Only categories relevant
for the detected technologies are scored. Each category starts at 100 and loses
points per finding: BLOCKER −45, MAJOR −15, MINOR −5, INFO −1. The overall
score is the average of the relevant categories.

## API

Contract-first OpenAPI spec: [`repo-governor-contracts/openapi.yaml`](repo-governor-contracts/openapi.yaml).
All endpoints require an `X-API-Key` header. Highlights:

- `POST /api/v1/scan-reports` — upload a scan report
- `GET /api/v1/repositories` / `GET /api/v1/repositories/{id}` — repo overview/detail
- `GET /api/v1/repositories/{id}/scans` / `GET /api/v1/repositories/{id}/findings?severity=&category=&status=`
- `GET /api/v1/scans/{id}` / `GET /api/v1/scans/{id}/diff` — scan detail and diff with previous scan
- `PATCH /api/v1/findings/{id}/status` — triage (OPEN / ACCEPTED_RISK / RESOLVED)
- `GET /api/v1/organizations/{id}/dashboard` — organization summary
- `GET /api/v1/rules` / `PATCH /api/v1/rules/{id}/configuration`

## Testing

```bash
./gradlew test        # unit tests (core rules, scoring, CLI) + ArchUnit
./gradlew build       # additionally runs Testcontainers integration tests (needs Docker)
cd repo-governor-web && npm run build
```

- Rule and scoring unit tests live in `repo-governor-core/src/test`.
- `ScanUploadFlowIT` spins up PostgreSQL with Testcontainers and exercises the
  full upload → list → triage → diff → dashboard flow.
- ArchUnit guards: controllers never touch Spring Data repositories or JPA
  entities; the server never depends on CLI/core code.

## Architecture notes

- The scanner never prints; rules return `Finding`s; reporting is decoupled
  from scanning; CLI orchestrates scanner + reporting + upload.
- File system access goes through the `RepoFiles` abstraction (disk and
  in-memory implementations), keeping rules trivially testable.
- The server stores scans per repository, carries finding triage status over to
  matching findings in newer scans, and computes diffs between consecutive scans.
- MVP authentication is an API key (`X-API-Key`) resolved to an organization;
  designed to be replaced by OAuth/OIDC later.
