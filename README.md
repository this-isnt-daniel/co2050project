# Forensic Medicine Department — Backend API

A complete Spring Boot 3.x + PostgreSQL 15 backend for a university Forensic Medicine Department database system,
replacing a manual paper-based record system. The system manages two operational streams (Clinical and Autopsy),
enforces strict RBAC, maintains court-defensible audit trails, and generates medico-legal PDF reports.

## Architecture

```
com.forensicdept.<module>
├── controller   → REST endpoints only, no business logic
├── service      → Business logic + @PreAuthorize (method-level RBAC)
├── repository   → Spring Data JPA interfaces
├── entity       → JPA entities (mapped to Flyway-managed tables)
└── dto          → Request/response DTOs (entities never exposed directly)
```

Cross-cutting packages: `security/`, `audit/`, `notification/`, `exception/`, `config/`

See [docs/erd.md](docs/erd.md) for the full Entity-Relationship diagram.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Build | Maven |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway 10.x (versioned SQL only — no `ddl-auto: update`) |
| Auth | Spring Security + JWT (JJWT 0.12.x), stateless |
| Validation | Jakarta Bean Validation |
| Boilerplate | Lombok + MapStruct |
| PDF | OpenPDF 2.x |
| Tests | JUnit 5 + Testcontainers (real PostgreSQL, not H2) |
| API Docs | springdoc-openapi 2.x (Swagger UI) |

---

## Quick Start — Docker Compose (Recommended)

### 1. Prerequisites

- Docker Desktop
- Git

### 2. Clone and Configure

```bash
git clone <repository-url>
cd co2050project
cp .env.example .env
# Edit .env — fill in real values (especially JWT_SECRET)
```

Minimum required changes in `.env`:
```
POSTGRES_PASSWORD=your_strong_password
DB_PASSWORD=your_strong_password
JWT_SECRET=your_64_char_hex_secret_here  # openssl rand -hex 64
```

### 3. Run the Full Stack

```bash
docker-compose up --build
```

This starts:
- `forensic_db` — PostgreSQL 15 on port 5432
- `forensic_app` — Spring Boot API on port 8080

Flyway automatically applies V1, V2, V3 migrations on first startup.

### 4. Verify

```bash
curl http://localhost:8080/actuator/health
# → {"status":"UP"}

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## Quick Start — Local Without Docker

### Prerequisites

- JDK 17+
- Maven 3.9+
- PostgreSQL 15 running locally

### 1. Create the database

```sql
CREATE DATABASE forensic_dept;
CREATE USER forensic_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE forensic_dept TO forensic_user;
```

### 2. Set environment variables

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=forensic_dept
export DB_USER=forensic_user
export DB_PASSWORD=your_password
export JWT_SECRET=your_64_char_hex_secret
export DOCUMENT_STORAGE_PATH=./data/documents
```

### 3. Run migrations and start

```bash
mvn spring-boot:run
```

Flyway runs automatically on startup.

---

## Running Migrations Manually

```bash
mvn flyway:migrate \
  -Dflyway.url=jdbc:postgresql://localhost:5432/forensic_dept \
  -Dflyway.user=forensic_user \
  -Dflyway.password=your_password
```

---

## Running Tests

```bash
mvn test
```

Tests run against a **real PostgreSQL 15** instance spun up by Testcontainers — no H2 mocking.
Requires Docker to be running.

---

## API Documentation

Once the app is running, visit:

```
http://localhost:8080/swagger-ui.html
```

Click **Authorize**, paste your JWT (from `POST /api/auth/login`), and explore all 13 endpoint groups.

---

## Seeded Credentials (V2 migration)

> **Password for all accounts: `Password@123`**

| Username | Role | Notes |
|---|---|---|
| `admin` | ADMIN | Full system access |
| `dr.perera` | DOCTOR | Dr. Amara Perera — Forensic Pathology |
| `dr.bandara` | DOCTOR | Dr. Nimal Bandara — Clinical Forensic Medicine |
| `jmo.suresh` | JMO | Judicial Medical Officer |
| `lab.kamala` | LAB_STAFF | Toxicology laboratory |
| `clerical.dilini` | CLERICAL | Administrative staff |
| `researcher01` | RESEARCHER | Read-only, de-identified access |

---

## Role-Based Access Control

| Role | Patients | Cases | MLEF | PM | Evidence | Lab Tests | Court Reports | Users |
|---|---|---|---|---|---|---|---|---|
| ADMIN | Full | Full | Full | Full | Full | Full | Full | Full |
| DOCTOR | Full | Own cases | Own cases | Own cases | Read | Read | Read | Own |
| JMO | Full | Full | Full | Full | Full | Read | Full | Own |
| LAB_STAFF | Read | Read | — | — | Read/Write | Full | — | Own |
| CLERICAL | Full | Read | Read | Read | — | — | Read | Own |
| RESEARCHER | **De-identified** | — | — | — | — | — | — | Own |

- Role checks are enforced via `@PreAuthorize` on **service methods**, not just controllers.
- RESEARCHER role receives a separate `PatientDeidentifiedResponse` DTO — PII is excluded at the type level.

---

## Sample Data (V3 migration)

Five cases covering all required categories:

| Case No. | Type | Manner / Category | Status |
|---|---|---|---|
| CW/01/24 | CLINICAL | Assault / Blunt trauma | REPORT_DRAFTED |
| PM/01/24 | AUTOPSY | **NATURAL** (liver cirrhosis) | SUBMITTED |
| PM/02/24 | AUTOPSY | **ACCIDENTAL** (RTA) | IN_PROGRESS |
| PM/03/24 | AUTOPSY | **SUICIDAL** (hanging) | REPORT_DRAFTED |
| PM/04/24 | AUTOPSY | **HOMICIDAL** (stab wounds) | OPEN |

---

## Backup & Recovery

### Backup (pg_dump)

```bash
# Full database backup
pg_dump -h localhost -U forensic_user -d forensic_dept \
  -Fc -f backup_$(date +%Y%m%d_%H%M%S).dump

# Via Docker container
docker exec forensic_db pg_dump \
  -U $POSTGRES_USER -Fc $POSTGRES_DB > backup_$(date +%Y%m%d).dump
```

### Restore

```bash
# From custom format dump
pg_restore -h localhost -U forensic_user -d forensic_dept \
  --clean --if-exists backup_20240711.dump

# Via Docker container
docker exec -i forensic_db pg_restore \
  -U $POSTGRES_USER -d $POSTGRES_DB < backup_20240711.dump
```

### Automated Backup (cron example)

```bash
# Add to crontab: daily backup at 2:00 AM
0 2 * * * docker exec forensic_db pg_dump -U forensic_user -Fc forensic_dept > /backups/forensic_$(date +\%Y\%m\%d).dump
```

---

## Direct SQL Queries for Viva Demo

```sql
-- All cases with patient and doctor names
SELECT c.case_number, c.case_type, c.case_status,
       p.full_name AS patient, s.name AS assigned_doctor
FROM cases c
JOIN patients p ON c.patient_id = p.id
JOIN staff s    ON c.assigned_doctor_id = s.id;

-- Chain of custody for evidence item #1
SELECT ecl.transfer_timestamp,
       sf.name AS from_staff,
       st.name AS to_staff,
       ecl.reason
FROM evidence_custody_log ecl
LEFT JOIN staff sf ON ecl.transferred_from = sf.id
LEFT JOIN staff st ON ecl.transferred_to   = st.id
WHERE ecl.evidence_id = 1
ORDER BY ecl.transfer_timestamp;

-- All autopsy cases by manner of death
SELECT pm.cause_of_death_category, COUNT(*) AS count
FROM postmortem pm
GROUP BY pm.cause_of_death_category;

-- Upcoming court dates
SELECT cr.case_id, c.case_number, cr.court_name, cr.date_of_trial
FROM court_reports cr
JOIN cases c ON cr.case_id = c.id
WHERE cr.date_of_trial BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '14 days';
```

---

## Known Limitations (MVP)

- **No refresh tokens** — JWT access tokens are short-lived (24h default). Documented limitation.
- **PDF template fidelity** — The MLR/PMR PDFs are structured but do not replicate the department's exact official forms. Full template fidelity is a follow-up item.
- **File storage** — Local filesystem only. The `StorageService` interface is designed for a drop-in S3 implementation.
- **Email/SMS notifications** — Notification records are created and stored. Delivery via email/SMS is out of scope for MVP.

---

## Project Structure

```
co2050project/
├── docs/
│   └── erd.md                          ← Entity-Relationship Diagram
├── src/
│   ├── main/
│   │   ├── java/com/forensicdept/
│   │   │   ├── ForensicDeptApplication.java
│   │   │   ├── audit/                  ← AuditLog entity + listener
│   │   │   ├── casemanagement/         ← Case module
│   │   │   ├── config/                 ← AppProperties, OpenAPI
│   │   │   ├── courtreport/            ← Court report module
│   │   │   ├── document/               ← Document + StorageService
│   │   │   ├── evidence/               ← Evidence + custody log
│   │   │   ├── exception/              ← GlobalExceptionHandler
│   │   │   ├── labtest/                ← Laboratory tests
│   │   │   ├── mlef/                   ← Medico-Legal Examination Form
│   │   │   ├── notification/           ← Notifications + scheduler
│   │   │   ├── patient/                ← Patient module (incl. de-id DTO)
│   │   │   ├── postmortem/             ← Postmortem module
│   │   │   ├── report/                 ← PDF report generation
│   │   │   ├── security/               ← JWT + Spring Security
│   │   │   ├── staff/                  ← Staff module
│   │   │   └── user/                   ← User + auth module
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           ├── V1__initial_schema.sql
│   │           ├── V2__reference_data.sql
│   │           └── V3__sample_data.sql
│   └── test/
│       └── java/com/forensicdept/
│           ├── PatientRepositoryTest.java
│           └── AuthIntegrationTest.java
├── .env.example
├── .gitignore
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```
