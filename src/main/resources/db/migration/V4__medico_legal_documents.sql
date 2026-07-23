-- =============================================================================
-- V4__medico_legal_documents.sql
-- Medico-Legal Document Handling Improvements
-- Adds: official serial numbers, MLEF referral fields, MLR table,
--       MLR revisions table, evidence/court-report additions,
--       document file-size, and the serial-sequence counter.
-- FK strategy: ON DELETE RESTRICT (consistent with V1)
-- =============================================================================

-- ---------------------------------------------------------------------------
-- DOCUMENT_SERIAL_SEQUENCES
-- Independent, year-keyed sequence counter per document type.
-- last_seq is the highest sequence number assigned in that year.
-- Never delete rows — gaps must not be reused.
-- ---------------------------------------------------------------------------
CREATE TABLE document_serial_sequences (
    doc_type  VARCHAR(10)  NOT NULL,
    year      INTEGER      NOT NULL,
    last_seq  INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT pk_doc_serial PRIMARY KEY (doc_type, year)
);

-- Seed rows for the current year so the first INSERT just increments.
INSERT INTO document_serial_sequences (doc_type, year, last_seq) VALUES
    ('CASE', 2026, 0),
    ('MLEF', 2026, 0),
    ('MLR',  2026, 0),
    ('EV',   2026, 0),
    ('CRT',  2026, 0);

-- ---------------------------------------------------------------------------
-- ALTER MLEF — add official serial + referral fields
-- ---------------------------------------------------------------------------
ALTER TABLE mlef
    ADD COLUMN mlef_number               VARCHAR(30)  UNIQUE,
    ADD COLUMN received_date             DATE,
    ADD COLUMN referring_hospital        VARCHAR(255),
    ADD COLUMN referring_medical_officer VARCHAR(255),
    ADD COLUMN police_station            VARCHAR(255),
    ADD COLUMN police_reference          VARCHAR(100),
    ADD COLUMN case_reference            VARCHAR(100);

CREATE INDEX idx_mlef_mlef_number ON mlef(mlef_number);

-- ---------------------------------------------------------------------------
-- MLR — Medico-Legal Report (distinct from Court Report)
-- One official MLR per case; amendments create a revision record.
-- ---------------------------------------------------------------------------
CREATE TABLE mlr (
    id                   BIGSERIAL     PRIMARY KEY,
    mlr_number           VARCHAR(30)   NOT NULL UNIQUE,
    case_id              BIGINT        NOT NULL REFERENCES cases(id) ON DELETE RESTRICT,
    prepared_by          BIGINT        NOT NULL REFERENCES staff(id) ON DELETE RESTRICT,
    examination_date     DATE,
    date_finalized       DATE,
    report_status        VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'
                                       CHECK (report_status IN ('DRAFT', 'FINALIZED')),
    digital_report_path  VARCHAR(500),
    created_at           TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_mlr_case UNIQUE (case_id)   -- one active MLR per case
);

CREATE INDEX idx_mlr_case_id       ON mlr(case_id);
CREATE INDEX idx_mlr_mlr_number    ON mlr(mlr_number);
CREATE INDEX idx_mlr_report_status ON mlr(report_status);
CREATE INDEX idx_mlr_prepared_by   ON mlr(prepared_by);

-- ---------------------------------------------------------------------------
-- MLR_REVISIONS — immutable snapshot of each MLR version before amendment
-- A revision is written BEFORE the MLR row is updated, preserving history.
-- ---------------------------------------------------------------------------
CREATE TABLE mlr_revisions (
    id                   BIGSERIAL     PRIMARY KEY,
    mlr_id               BIGINT        NOT NULL REFERENCES mlr(id) ON DELETE RESTRICT,
    revision_number      INTEGER       NOT NULL,
    report_status_at_revision VARCHAR(20) NOT NULL,
    digital_report_path  VARCHAR(500),
    revised_by           BIGINT        REFERENCES staff(id) ON DELETE RESTRICT,
    revision_reason      TEXT,
    created_at           TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_mlr_revision UNIQUE (mlr_id, revision_number)
);

CREATE INDEX idx_mlr_revisions_mlr_id ON mlr_revisions(mlr_id);

-- ---------------------------------------------------------------------------
-- ALTER EVIDENCE — add official serial, enforce NOT NULL on description
-- ---------------------------------------------------------------------------
ALTER TABLE evidence
    ADD COLUMN evidence_number VARCHAR(30) UNIQUE;

-- Make description NOT NULL for future rows (existing NULLs get a placeholder).
UPDATE evidence SET description = '(no description)' WHERE description IS NULL;
ALTER TABLE evidence ALTER COLUMN description SET NOT NULL;

CREATE INDEX idx_evidence_evidence_number ON evidence(evidence_number);

-- ---------------------------------------------------------------------------
-- ALTER COURT_REPORTS — add official serial + court metadata + prepared_by
-- ---------------------------------------------------------------------------
ALTER TABLE court_reports
    ADD COLUMN court_report_number VARCHAR(30)  UNIQUE,
    ADD COLUMN court_case_number   VARCHAR(100),
    ADD COLUMN requested_date      DATE,
    ADD COLUMN prepared_by         BIGINT       REFERENCES staff(id) ON DELETE RESTRICT;

CREATE INDEX idx_court_reports_court_report_number ON court_reports(court_report_number);
CREATE INDEX idx_court_reports_prepared_by         ON court_reports(prepared_by);

-- ---------------------------------------------------------------------------
-- ALTER DOCUMENTS — add file size metadata
-- ---------------------------------------------------------------------------
ALTER TABLE documents
    ADD COLUMN file_size_bytes BIGINT;

-- ---------------------------------------------------------------------------
-- BACK-FILL serial numbers for existing MLEF records
-- Uses a numbered CTE to assign incremental serials from 1.
-- The year is taken from created_at where available, defaulting to 2026.
-- document_serial_sequences is also updated to reflect the highest assigned.
-- ---------------------------------------------------------------------------
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (ORDER BY id ASC) AS rn,
           EXTRACT(YEAR FROM created_at)::INTEGER AS yr
    FROM mlef
    WHERE mlef_number IS NULL
)
UPDATE mlef
SET mlef_number = 'MLEF/' || r.yr || '/' || LPAD(r.rn::TEXT, 6, '0')
FROM ranked r
WHERE mlef.id = r.id;

-- Sync the sequence counter to the highest assigned value for 2026.
INSERT INTO document_serial_sequences (doc_type, year, last_seq)
SELECT 'MLEF', 2026, COUNT(*) FROM mlef WHERE mlef_number LIKE 'MLEF/2026/%'
ON CONFLICT (doc_type, year) DO UPDATE
    SET last_seq = EXCLUDED.last_seq;

-- ---------------------------------------------------------------------------
-- BACK-FILL serial numbers for existing EVIDENCE records
-- ---------------------------------------------------------------------------
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (ORDER BY id ASC) AS rn,
           EXTRACT(YEAR FROM created_at)::INTEGER AS yr
    FROM evidence
    WHERE evidence_number IS NULL
)
UPDATE evidence
SET evidence_number = 'EV/' || r.yr || '/' || LPAD(r.rn::TEXT, 6, '0')
FROM ranked r
WHERE evidence.id = r.id;

INSERT INTO document_serial_sequences (doc_type, year, last_seq)
SELECT 'EV', 2026, COUNT(*) FROM evidence WHERE evidence_number LIKE 'EV/2026/%'
ON CONFLICT (doc_type, year) DO UPDATE
    SET last_seq = EXCLUDED.last_seq;

-- ---------------------------------------------------------------------------
-- BACK-FILL serial numbers for existing COURT_REPORTS records
-- ---------------------------------------------------------------------------
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (ORDER BY id ASC) AS rn,
           EXTRACT(YEAR FROM created_at)::INTEGER AS yr
    FROM court_reports
    WHERE court_report_number IS NULL
)
UPDATE court_reports
SET court_report_number = 'CRT/' || r.yr || '/' || LPAD(r.rn::TEXT, 6, '0')
FROM ranked r
WHERE court_reports.id = r.id;

INSERT INTO document_serial_sequences (doc_type, year, last_seq)
SELECT 'CRT', 2026, COUNT(*) FROM court_reports WHERE court_report_number LIKE 'CRT/2026/%'
ON CONFLICT (doc_type, year) DO UPDATE
    SET last_seq = EXCLUDED.last_seq;
