package com.forensicdept.mlr.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MlrRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Prepared-by staff ID is required")
    private Long preparedById;

    private LocalDate examinationDate;
    private String digitalReportPath;

    @Pattern(regexp = "DRAFT|FINALIZED",
             message = "Report status must be DRAFT or FINALIZED")
    private String reportStatus;
}
