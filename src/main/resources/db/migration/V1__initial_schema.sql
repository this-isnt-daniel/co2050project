-- =============================================================================
-- V1__initial_schema.sql
-- Forensic Medicine Department — Initial Database Schema
-- Author: System
-- Normalization: 3NF minimum
-- FK strategy: ON DELETE RESTRICT (medico-legal records must never cascade-delete)
-- =============================================================================

-- ---------------------------------------------------------------------------
-- STAFF — must be created before users (users.staff_id → staff.id)
-- ---------------------------------------------------------------------------
CREATE TABLE staff (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255)  NOT NULL,
    staff_role    VARCHAR(50)   NOT NULL CHECK (staff_role IN ('DOCTOR','JMO','LAB_STAFF','CLERICAL','ADMIN')),
    contact_no    VARCHAR(30),
    specialization VARCHAR(255),
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- USERS — authentication accounts
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    user_role     VARCHAR(50)   NOT NULL CHECK (user_role IN ('ADMIN','DOCTOR','JMO','LAB_STAFF','CLERICAL','RESEARCHER')),
    staff_id      BIGINT        REFERENCES staff(id) ON DELETE RESTRICT,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- PATIENTS
-- ---------------------------------------------------------------------------
CREATE TABLE patients (
    id              BIGSERIAL PRIMARY KEY,
    full_name       VARCHAR(255)  NOT NULL,
    age             INTEGER       CHECK (age >= 0 AND age <= 150),
    gender          VARCHAR(20)   CHECK (gender IN ('MALE','FEMALE','OTHER','UNKNOWN')),
    address         TEXT,
    nic_passport_no VARCHAR(50)   UNIQUE,
    contact_info    VARCHAR(255),
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- CASES
-- ---------------------------------------------------------------------------
CREATE TABLE cases (
    id                  BIGSERIAL PRIMARY KEY,
    case_number         VARCHAR(50)   NOT NULL UNIQUE,  -- e.g. CW/01/24
    case_type           VARCHAR(20)   NOT NULL CHECK (case_type IN ('CLINICAL','AUTOPSY')),
    patient_id          BIGINT        REFERENCES patients(id) ON DELETE RESTRICT,
    incident_date       DATE,
    referring_authority VARCHAR(255),
    referred_by         VARCHAR(50)   CHECK (referred_by IN ('POLICE','HOSPITAL','COURT','OTHER')),
    case_status         VARCHAR(50)   NOT NULL DEFAULT 'OPEN'
                                      CHECK (case_status IN ('OPEN','IN_PROGRESS','REPORT_DRAFTED','SUBMITTED','CLOSED')),
    assigned_doctor_id  BIGINT        REFERENCES staff(id) ON DELETE RESTRICT,
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- MLEF — Medico-Legal Examination Form (clinical stream)
-- ---------------------------------------------------------------------------
CREATE TABLE mlef (
    id                       BIGSERIAL PRIMARY KEY,
    case_id                  BIGINT       NOT NULL REFERENCES cases(id) ON DELETE RESTRICT,
    examining_doctor_id      BIGINT       NOT NULL REFERENCES staff(id) ON DELETE RESTRICT,
    date_of_issue            DATE,
    examination_date_time    TIMESTAMP,
    nature_of_bodily_harm    TEXT,
    causative_weapon         VARCHAR(255),
    alcohol_drug_test_results TEXT,
    findings                 TEXT,
    report_status            VARCHAR(50)  NOT NULL DEFAULT 'DRAFT'
                                          CHECK (report_status IN ('DRAFT','ISSUED','PENDING_COURT_DATE')),
    created_at               TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_mlef_case UNIQUE (case_id)  -- one MLEF per clinical case
);

-- ---------------------------------------------------------------------------
-- POSTMORTEM — autopsy stream
-- ---------------------------------------------------------------------------
CREATE TABLE postmortem (
    id                       BIGSERIAL PRIMARY KEY,
    case_id                  BIGINT       NOT NULL REFERENCES cases(id) ON DELETE RESTRICT,
    doctor_id                BIGINT       NOT NULL REFERENCES staff(id) ON DELETE RESTRICT,
    inquest_order_ref        VARCHAR(255),
    inquest_date             DATE,
    place_of_pm              VARCHAR(255),
    cause_of_death_category  VARCHAR(50)  CHECK (cause_of_death_category IN ('NATURAL','ACCIDENTAL','SUICIDAL','HOMICIDAL')),
    findings                 TEXT,
    cause_of_death           TEXT,
    created_at               TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_postmortem_case UNIQUE (case_id)  -- one PM per autopsy case
);

-- ---------------------------------------------------------------------------
-- EVIDENCE
-- ---------------------------------------------------------------------------
CREATE TABLE evidence (
    id               BIGSERIAL PRIMARY KEY,
    case_id          BIGINT       NOT NULL REFERENCES cases(id) ON DELETE RESTRICT,
    evidence_type    VARCHAR(100) NOT NULL,
    description      TEXT,
    storage_location VARCHAR(255),
    collected_by     BIGINT       REFERENCES staff(id) ON DELETE RESTRICT,
    collected_at     TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- EVIDENCE CUSTODY LOG — chain of custody (court-defensible)
-- ---------------------------------------------------------------------------
CREATE TABLE evidence_custody_log (
    id                  BIGSERIAL PRIMARY KEY,
    evidence_id         BIGINT       NOT NULL REFERENCES evidence(id) ON DELETE RESTRICT,
    transferred_from    BIGINT       REFERENCES staff(id) ON DELETE RESTRICT,
    transferred_to      BIGINT       REFERENCES staff(id) ON DELETE RESTRICT,
    transfer_timestamp  TIMESTAMP    NOT NULL DEFAULT NOW(),
    reason              TEXT,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- LABORATORY TESTS
-- ---------------------------------------------------------------------------
CREATE TABLE laboratory_tests (
    id           BIGSERIAL PRIMARY KEY,
    case_id      BIGINT       NOT NULL REFERENCES cases(id) ON DELETE RESTRICT,
    test_type    VARCHAR(100) NOT NULL,
    requested_by BIGINT       REFERENCES staff(id) ON DELETE RESTRICT,
    result       TEXT,
    result_date  DATE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- COURT REPORTS
-- ---------------------------------------------------------------------------
CREATE TABLE court_reports (
    id                        BIGSERIAL PRIMARY KEY,
    case_id                   BIGINT       NOT NULL REFERENCES cases(id) ON DELETE RESTRICT,
    report_type               VARCHAR(20)  NOT NULL CHECK (report_type IN ('MLR','PMR')),
    submission_date           DATE,
    report_status             VARCHAR(50)  NOT NULL DEFAULT 'DRAFT'
                                            CHECK (report_status IN ('DRAFT','ISSUED','PENDING_COURT_DATE')),
    court_name                VARCHAR(255),
    date_of_trial             DATE,
    certificate_of_receipt_ref VARCHAR(255),
    created_at                TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- DOCUMENTS — file metadata only (no BLOBs in DB)
-- ---------------------------------------------------------------------------
CREATE TABLE documents (
    id           BIGSERIAL PRIMARY KEY,
    owner_type   VARCHAR(50)   NOT NULL CHECK (owner_type IN ('MLEF','POSTMORTEM','COURT_REPORT','CASE')),
    owner_id     BIGINT        NOT NULL,
    file_name    VARCHAR(255)  NOT NULL,
    file_type    VARCHAR(50),
    storage_path VARCHAR(500)  NOT NULL,
    uploaded_by  BIGINT        REFERENCES users(id) ON DELETE RESTRICT,
    uploaded_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- AUDIT LOGS
-- ---------------------------------------------------------------------------
CREATE TABLE audit_logs (
    id             BIGSERIAL PRIMARY KEY,
    entity_name    VARCHAR(100)  NOT NULL,
    entity_id      BIGINT        NOT NULL,
    action         VARCHAR(20)   NOT NULL CHECK (action IN ('CREATE','UPDATE','DELETE')),
    performed_by   VARCHAR(100),
    performed_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    change_summary JSONB
);

-- ---------------------------------------------------------------------------
-- NOTIFICATIONS
-- ---------------------------------------------------------------------------
CREATE TABLE notifications (
    id               BIGSERIAL PRIMARY KEY,
    notification_type VARCHAR(50)  NOT NULL
                                   CHECK (notification_type IN ('MLEF_PENDING','COD_PENDING','COURT_DATE_UPCOMING')),
    related_case_id  BIGINT        REFERENCES cases(id) ON DELETE RESTRICT,
    target_user_id   BIGINT        REFERENCES users(id) ON DELETE RESTRICT,
    message          TEXT          NOT NULL,
    notification_status VARCHAR(20) NOT NULL DEFAULT 'UNREAD'
                                    CHECK (notification_status IN ('UNREAD','READ','SENT')),
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- =============================================================================
-- INDEXES — query-hot columns
-- =============================================================================
CREATE INDEX idx_cases_case_number        ON cases(case_number);
CREATE INDEX idx_cases_status             ON cases(case_status);
CREATE INDEX idx_cases_assigned_doctor    ON cases(assigned_doctor_id);
CREATE INDEX idx_cases_patient            ON cases(patient_id);
CREATE INDEX idx_cases_case_type          ON cases(case_type);
CREATE INDEX idx_patients_name            ON patients(full_name);
CREATE INDEX idx_patients_nic             ON patients(nic_passport_no);
CREATE INDEX idx_mlef_case_id             ON mlef(case_id);
CREATE INDEX idx_mlef_report_status       ON mlef(report_status);
CREATE INDEX idx_mlef_examining_doctor    ON mlef(examining_doctor_id);
CREATE INDEX idx_postmortem_case_id       ON postmortem(case_id);
CREATE INDEX idx_postmortem_category      ON postmortem(cause_of_death_category);
CREATE INDEX idx_evidence_case_id         ON evidence(case_id);
CREATE INDEX idx_custody_log_evidence     ON evidence_custody_log(evidence_id);
CREATE INDEX idx_lab_tests_case_id        ON laboratory_tests(case_id);
CREATE INDEX idx_court_reports_case       ON court_reports(case_id);
CREATE INDEX idx_court_reports_trial_date ON court_reports(date_of_trial);
CREATE INDEX idx_court_reports_status     ON court_reports(report_status);
CREATE INDEX idx_documents_owner          ON documents(owner_type, owner_id);
CREATE INDEX idx_audit_logs_entity        ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_logs_performed_at  ON audit_logs(performed_at);
CREATE INDEX idx_notifications_user       ON notifications(target_user_id, notification_status);
CREATE INDEX idx_notifications_case       ON notifications(related_case_id);
