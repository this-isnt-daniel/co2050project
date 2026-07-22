# Basic Entity-Relationship (ER) Diagram

This diagram represents the conceptual structure of the database. It focuses purely on the entities and their attributes, without specifying physical database constraints or SQL data types.

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "primaryColor": "#ffffff",
    "primaryBorderColor": "#000000",
    "primaryTextColor": "#000000",
    "lineColor": "#000000",
    "tertiaryColor": "#ffffff",
    "fontFamily": "arial"
  }
}}%%
classDiagram
    class STAFF {
        id
        name
        staff_role
        contact_no
        specialization
        is_active
        created_at
        updated_at
    }

    class USERS {
        id
        username
        password_hash
        user_role
        staff_id
        is_active
        created_at
        updated_at
    }

    class PATIENTS {
        id
        full_name
        age
        gender
        address
        nic_passport_no
        contact_info
        created_at
        updated_at
    }

    class CASES {
        id
        case_number
        case_type
        patient_id
        incident_date
        referring_authority
        referred_by
        case_status
        assigned_doctor_id
        created_at
        updated_at
    }

    class MLEF {
        id
        case_id
        examining_doctor_id
        date_of_issue
        examination_date_time
        nature_of_bodily_harm
        causative_weapon
        alcohol_drug_test_results
        findings
        report_status
        created_at
        updated_at
    }

    class POSTMORTEM {
        id
        case_id
        doctor_id
        inquest_order_ref
        inquest_date
        place_of_pm
        cause_of_death_category
        findings
        cause_of_death
        created_at
        updated_at
    }

    class EVIDENCE {
        id
        case_id
        evidence_type
        description
        storage_location
        collected_by
        collected_at
        created_at
        updated_at
    }

    class EVIDENCE_CUSTODY_LOG {
        id
        evidence_id
        transferred_from
        transferred_to
        transfer_timestamp
        reason
        created_at
    }

    class LABORATORY_TESTS {
        id
        case_id
        test_type
        requested_by
        result
        result_date
        created_at
        updated_at
    }

    class COURT_REPORTS {
        id
        case_id
        report_type
        submission_date
        report_status
        court_name
        date_of_trial
        certificate_of_receipt_ref
        created_at
        updated_at
    }

    class DOCUMENTS {
        id
        owner_type
        owner_id
        file_name
        file_type
        storage_path
        uploaded_by
        uploaded_at
        created_at
    }

    class AUDIT_LOGS {
        id
        entity_name
        entity_id
        action
        performed_by
        performed_at
        change_summary
    }

    class NOTIFICATIONS {
        id
        notification_type
        related_case_id
        target_user_id
        message
        notification_status
        created_at
    }

    %% Relationships
    STAFF "1" -- "*" USERS : has account
    PATIENTS "1" -- "*" CASES : is subject of
    STAFF "1" -- "*" CASES : assigned as doctor
    CASES "1" -- "0..1" MLEF : has one MLEF
    CASES "1" -- "0..1" POSTMORTEM : has one PM
    STAFF "1" -- "*" MLEF : examining doctor
    STAFF "1" -- "*" POSTMORTEM : pathologist
    CASES "1" -- "*" EVIDENCE : has evidence
    STAFF "1" -- "*" EVIDENCE : collected by
    EVIDENCE "1" -- "*" EVIDENCE_CUSTODY_LOG : custody chain
    STAFF "1" -- "*" EVIDENCE_CUSTODY_LOG : transferred from
    STAFF "1" -- "*" EVIDENCE_CUSTODY_LOG : transferred to
    CASES "1" -- "*" LABORATORY_TESTS : has lab tests
    STAFF "1" -- "*" LABORATORY_TESTS : requested by
    CASES "1" -- "*" COURT_REPORTS : has court reports
    USERS "1" -- "*" DOCUMENTS : uploaded by
    CASES "1" -- "*" NOTIFICATIONS : related case
    USERS "1" -- "*" NOTIFICATIONS : target user
```
