package com.forensicdept.casemanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CaseResponse {
    private Long id;
    private String caseNumber;
    private String caseType;
    private Long patientId;
    private String patientName;
    private LocalDate incidentDate;
    private String referringAuthority;
    private String referredBy;
    private String caseStatus;
    private Long assignedDoctorId;
    private String assignedDoctorName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
