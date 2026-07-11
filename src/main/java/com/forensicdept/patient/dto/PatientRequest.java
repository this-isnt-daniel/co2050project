package com.forensicdept.patient.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PatientRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String fullName;

    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 150, message = "Age is unrealistic")
    private Integer age;

    @Pattern(regexp = "MALE|FEMALE|OTHER|UNKNOWN",
             message = "Gender must be one of: MALE, FEMALE, OTHER, UNKNOWN")
    private String gender;

    private String address;

    @Size(max = 50, message = "NIC/Passport must not exceed 50 characters")
    private String nicPassportNo;

    @Size(max = 255, message = "Contact info must not exceed 255 characters")
    private String contactInfo;
}
