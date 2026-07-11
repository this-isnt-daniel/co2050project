package com.forensicdept.postmortem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PostmortemRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    private String inquestOrderRef;
    private LocalDate inquestDate;
    private String placeOfPm;

    @Pattern(regexp = "NATURAL|ACCIDENTAL|SUICIDAL|HOMICIDAL",
             message = "Cause of death category must be NATURAL, ACCIDENTAL, SUICIDAL, or HOMICIDAL")
    private String causeOfDeathCategory;

    private String findings;
    private String causeOfDeath;
}
