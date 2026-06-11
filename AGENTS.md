# Agent instructions for Repo Governor

Monorepo with four modules:

| Module | Stack | Purpose |
|---|---|---|
| `repo-governor-core` | Plain Java 21 (no Spring) | Domain model, scan rules, scoring, JSON/Markdown reporting |
| `repo-governor-cli` | Picocli | `init`, `scan`, `report`, `upload`, `rules list` commands |
| `repo-governor-server` | Spring Boot 4, PostgreSQL, Flyway | REST API (`/api/v1/**`), API-key auth, dashboard aggregation |
| `repo-governor-web` | Angular 21, Material, signals | SaaS dashboard |

`repo-governor-contracts/openapi.yaml` is the contract-first API definition.

## Build & test

```bash
./gradlew build                 # all Java modules; Testcontainers ITs need Docker
cd repo-governor-web && npm ci && npm run build
```

## Conventions

- Core must stay free of Spring dependencies; the server must never depend on
  core or CLI code (enforced by ArchUnit in `repo-governor-server`).
- Rules return `Finding`s and never print; reporting is separate from scanning.
- File system access in rules goes through the `RepoFiles` abstraction so rules
  are unit-testable (`InMemoryRepoFiles`).
- API boundaries use DTO records, never JPA entities (enforced by ArchUnit).
- Angular: standalone components, signals for state, all HTTP calls go through
  `shared/api/api-client.service.ts`.
- When adding a rule: implement it in core, register it in `Rules.defaultRules()`,
  add a row to the server's rule seed migration, and write a unit test.
