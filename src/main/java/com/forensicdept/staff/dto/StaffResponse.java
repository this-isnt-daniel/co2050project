package com.forensicdept.staff.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffResponse {
    private Long id;
    private String name;
    private String staffRole;
    private String contactNo;
    private String specialization;
    private Boolean isActive;
}
