# Enhanced Entity-Relationship (EER) Diagram

This diagram visualizes the generalization, specialization, and inheritance hierarchies within the system. It groups common attributes into Superclasses (`PERSON` and `CASE_REPORT`) and leaves unique attributes in Subclasses.

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
    class PERSON {
        <<Superclass>>
        id
        full_name
        contact_info
    }
    
    class PATIENT {
        <<Subclass>>
        age
        gender
        nic_passport_no
    }
    
    class STAFF {
        <<Subclass>>
        staff_role
        specialization
    }
    
    PERSON <|-- PATIENT
    PERSON <|-- STAFF

    class CASE_REPORT {
        <<Superclass>>
        id
        case_id
        created_at
    }
    
    class MLEF {
        <<Subclass>>
        examining_doctor_id
        bodily_harm
        findings
    }
    
    class POSTMORTEM {
        <<Subclass>>
        doctor_id
        cause_of_death
    }
    
    class LABORATORY_TEST {
        <<Subclass>>
        test_type
        result
    }
    
    class COURT_REPORT {
        <<Subclass>>
        report_type
        court_name
    }
    
    CASE_REPORT <|-- MLEF
    CASE_REPORT <|-- POSTMORTEM
    CASE_REPORT <|-- LABORATORY_TEST
    CASE_REPORT <|-- COURT_REPORT

    class CASES {
        id
        case_number
        case_type
    }

    PATIENT "1" -- "*" CASES : is subject of
    STAFF "1" -- "*" CASES : assigned as doctor
    CASES "1" *-- "*" CASE_REPORT : contains
```
