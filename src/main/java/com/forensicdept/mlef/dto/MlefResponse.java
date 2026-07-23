package com.forensicdept.mlef.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class MlefResponse {
    // Official serial number — never changes after creation
    private String mlefNumber;

    private Long id;
    private Long caseId;
    private String caseNumber;
    private Long examiningDoctorId;
    private String examiningDoctorName;

    // Referral / receipt fields
    private LocalDate dateOfIssue;
    private LocalDate receivedDate;
    private String referringHospital;
    private String referringMedicalOfficer;
    private String policeStation;
    private String policeReference;
    private String caseReference;

    // Examination findings
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime examinationDateTime;
    private String natureOfBodilyHarm;
    private String causativeWeapon;
    private String alcoholDrugTestResults;
    private String findings;
    private String reportStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
