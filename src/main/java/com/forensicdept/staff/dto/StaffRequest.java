package com.forensicdept.staff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StaffRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Staff role is required")
    @Pattern(regexp = "DOCTOR|JMO|LAB_STAFF|CLERICAL|ADMIN",
             message = "Staff role must be one of: DOCTOR, JMO, LAB_STAFF, CLERICAL, ADMIN")
    private String staffRole;

    @Size(max = 30, message = "Contact number must not exceed 30 characters")
    private String contactNo;

    @Size(max = 255, message = "Specialization must not exceed 255 characters")
    private String specialization;
}
