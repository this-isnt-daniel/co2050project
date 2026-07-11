package com.forensicdept.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|DOCTOR|JMO|LAB_STAFF|CLERICAL|RESEARCHER",
             message = "Role must be one of: ADMIN, DOCTOR, JMO, LAB_STAFF, CLERICAL, RESEARCHER")
    private String userRole;

    private Long staffId;  // nullable for RESEARCHER role
}
