package com.forensicdept.labtest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LaboratoryTestRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotBlank(message = "Test type is required")
    private String testType;

    private Long requestedById;
    private String result;
    private LocalDate resultDate;
}
