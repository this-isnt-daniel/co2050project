package com.forensicdept.evidence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EvidenceRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotBlank(message = "Evidence type is required")
    private String evidenceType;

    private String description;
    private String storageLocation;
    private Long collectedById;
    private LocalDateTime collectedAt;
}
