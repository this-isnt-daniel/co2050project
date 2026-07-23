package com.forensicdept.mlr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MlrResponse {
    /** Official serial, e.g. MLR/2026/000001 */
    private String mlrNumber;

    private Long id;
    private Long caseId;
    private String caseNumber;
    private Long preparedById;
    private String preparedByName;
    private LocalDate examinationDate;
    private LocalDate dateFinalized;
    private String reportStatus;
    private String digitalReportPath;

    /** Full revision history, oldest first. */
    private List<MlrRevisionResponse> revisions;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
