package com.forensicdept.casemanagement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CaseRequest {

    @NotBlank(message = "Case number is required")
    @Size(max = 50, message = "Case number must not exceed 50 characters")
    private String caseNumber;

    @NotBlank(message = "Case type is required")
    @Pattern(regexp = "CLINICAL|AUTOPSY", message = "Case type must be CLINICAL or AUTOPSY")
    private String caseType;

    private Long patientId;

    private LocalDate incidentDate;

    @Size(max = 255)
    private String referringAuthority;

    @Pattern(regexp = "POLICE|HOSPITAL|COURT|OTHER",
             message = "Referred by must be one of: POLICE, HOSPITAL, COURT, OTHER")
    private String referredBy;

    private Long assignedDoctorId;
}
