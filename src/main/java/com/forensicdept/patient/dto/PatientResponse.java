package com.forensicdept.patient.dto;

import lombok.Builder;
import lombok.Data;

/** Full patient response — accessible to ADMIN, DOCTOR, JMO, CLERICAL. */
@Data
@Builder
public class PatientResponse {
    private Long id;
    private String fullName;
    private Integer age;
    private String gender;
    private String address;
    private String nicPassportNo;
    private String contactInfo;
}
