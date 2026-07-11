package com.forensicdept.courtreport.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CourtReportResponse {
    private Long id;
    private Long caseId;
    private String caseNumber;
    private String reportType;
    private LocalDate submissionDate;
    private String reportStatus;
    private String courtName;
    private LocalDate dateOfTrial;
    private String certificateOfReceiptRef;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
