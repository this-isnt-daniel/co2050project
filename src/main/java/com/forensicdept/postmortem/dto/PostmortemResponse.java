package com.forensicdept.postmortem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PostmortemResponse {
    private Long id;
    private Long caseId;
    private String caseNumber;
    private Long doctorId;
    private String doctorName;
    private String inquestOrderRef;
    private LocalDate inquestDate;
    private String placeOfPm;
    private String causeOfDeathCategory;
    private String findings;
    private String causeOfDeath;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
