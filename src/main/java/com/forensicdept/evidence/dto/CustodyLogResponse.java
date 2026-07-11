package com.forensicdept.evidence.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustodyLogResponse {
    private Long id;
    private Long transferredFromId;
    private String transferredFromName;
    private Long transferredToId;
    private String transferredToName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime transferTimestamp;
    private String reason;
}
