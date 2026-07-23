package com.forensicdept.mlr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MlrRevisionResponse {
    private Long id;
    private Integer revisionNumber;
    private String reportStatusAtRevision;
    private String digitalReportPath;
    private Long revisedById;
    private String revisedByName;
    private String revisionReason;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
