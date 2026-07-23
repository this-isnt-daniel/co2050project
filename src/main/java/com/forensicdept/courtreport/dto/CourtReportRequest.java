package com.forensicdept.courtreport.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CourtReportRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Report type is required")
    @Pattern(regexp = "MLR|PMR|MLEF", message = "Report type must be MLR, PMR, or MLEF")
    private String reportType;

    private LocalDate submissionDate;
    private LocalDate requestedDate;

    @Pattern(regexp = "DRAFT|ISSUED|PENDING_COURT_DATE",
             message = "Report status must be DRAFT, ISSUED, or PENDING_COURT_DATE")
    private String reportStatus;

    private String courtName;
    private String courtCaseNumber;
    private LocalDate dateOfTrial;
    private String certificateOfReceiptRef;
    private Long preparedById;
}
