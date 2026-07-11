package com.forensicdept.mlef.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class MlefResponse {
    private Long id;
    private Long caseId;
    private String caseNumber;
    private Long examiningDoctorId;
    private String examiningDoctorName;
    private LocalDate dateOfIssue;
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
