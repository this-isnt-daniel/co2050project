package com.forensicdept.evidence.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EvidenceResponse {
    private Long id;
    private Long caseId;
    private String evidenceType;
    private String description;
    private String storageLocation;
    private Long collectedById;
    private String collectedByName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime collectedAt;
    private List<CustodyLogResponse> custodyLog;
}
