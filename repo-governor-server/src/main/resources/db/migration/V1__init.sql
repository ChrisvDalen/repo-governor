CREATE TABLE organizations (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE teams (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id),
    name            TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (organization_id, name)
);

CREATE TABLE repositories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id),
    team_id         UUID REFERENCES teams (id),
    name            TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (organization_id, name)
);

CREATE TABLE scans (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    repository_id UUID NOT NULL REFERENCES repositories (id),
    branch        TEXT,
    commit_hash   TEXT,
    scanned_at    TIMESTAMPTZ NOT NULL,
    tool_version  TEXT NOT NULL,
    overall_score INT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_scans_repository_scanned_at ON scans (repository_id, scanned_at DESC);

CREATE TABLE scan_category_scores (
    scan_id  UUID NOT NULL REFERENCES scans (id) ON DELETE CASCADE,
    category TEXT NOT NULL,
    score    INT NOT NULL
);

CREATE TABLE scan_technologies (
    scan_id    UUID NOT NULL REFERENCES scans (id) ON DELETE CASCADE,
    technology TEXT NOT NULL,
    position   INT NOT NULL
);

CREATE TABLE scan_metadata (
    scan_id    UUID NOT NULL REFERENCES scans (id) ON DELETE CASCADE,
    meta_key   TEXT NOT NULL,
    meta_value TEXT
);

CREATE TABLE findings (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scan_id        UUID NOT NULL REFERENCES scans (id) ON DELETE CASCADE,
    repository_id  UUID NOT NULL REFERENCES repositories (id),
    rule_id        TEXT NOT NULL,
    title          TEXT NOT NULL,
    description    TEXT,
    severity       TEXT NOT NULL,
    category       TEXT NOT NULL,
    file_path      TEXT,
    line_number    INT,
    recommendation TEXT,
    status         TEXT NOT NULL DEFAULT 'OPEN',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_findings_scan ON findings (scan_id);
CREATE INDEX idx_findings_repository ON findings (repository_id);

CREATE TABLE rules (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_key    TEXT NOT NULL UNIQUE,
    title       TEXT NOT NULL,
    description TEXT,
    category    TEXT NOT NULL,
    severity    TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE rule_configurations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_id         UUID NOT NULL REFERENCES rules (id),
    organization_id UUID NOT NULL REFERENCES organizations (id),
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    threshold       INT,
    UNIQUE (rule_id, organization_id)
);

CREATE TABLE api_keys (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id),
    key_value       TEXT NOT NULL UNIQUE,
    name            TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
