package com.forensicdept.mlef.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MlefRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Examining doctor ID is required")
    private Long examiningDoctorId;

    private LocalDate dateOfIssue;
    private LocalDateTime examinationDateTime;
    private String natureOfBodilyHarm;
    private String causativeWeapon;
    private String alcoholDrugTestResults;
    private String findings;

    @Pattern(regexp = "DRAFT|ISSUED|PENDING_COURT_DATE",
             message = "Report status must be DRAFT, ISSUED, or PENDING_COURT_DATE")
    private String reportStatus;
}
