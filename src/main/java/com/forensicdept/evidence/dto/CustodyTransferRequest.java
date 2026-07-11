package com.forensicdept.evidence.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustodyTransferRequest {

    @NotNull(message = "Evidence ID is required")
    private Long evidenceId;

    private Long transferredFromId;

    @NotNull(message = "Transferred to is required")
    private Long transferredToId;

    private LocalDateTime transferTimestamp;

    private String reason;
}
