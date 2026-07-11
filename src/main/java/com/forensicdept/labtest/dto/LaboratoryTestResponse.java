package com.forensicdept.labtest.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LaboratoryTestResponse {
    private Long id;
    private Long caseId;
    private String caseNumber;
    private String testType;
    private Long requestedById;
    private String requestedByName;
    private String result;
    private LocalDate resultDate;
}
