package com.forensicdept.patient.dto;

import lombok.Builder;
import lombok.Data;

/**
 * De-identified patient response for RESEARCHER role.
 * Omits fullName and nicPassportNo — implemented as a separate DTO, not a runtime field filter.
 */
@Data
@Builder
public class PatientDeidentifiedResponse {
    private Long id;
    private Integer age;
    private String gender;
    // Deliberately excludes: fullName, nicPassportNo, address, contactInfo
}
