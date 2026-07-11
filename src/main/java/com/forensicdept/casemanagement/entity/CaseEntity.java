package com.forensicdept.casemanagement.entity;

import com.forensicdept.audit.AuditEntityListener;
import com.forensicdept.audit.Auditable;
import com.forensicdept.patient.entity.PatientEntity;
import com.forensicdept.staff.entity.StaffEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cases")
@EntityListeners(AuditEntityListener.class)
@Auditable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", nullable = false, unique = true, length = 50)
    private String caseNumber;

    @Column(name = "case_type", nullable = false, length = 20)
    private String caseType;      // CLINICAL | AUTOPSY

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private PatientEntity patient;

    @Column(name = "incident_date")
    private LocalDate incidentDate;

    @Column(name = "referring_authority", length = 255)
    private String referringAuthority;

    @Column(name = "referred_by", length = 50)
    private String referredBy;    // POLICE | HOSPITAL | COURT | OTHER

    @Column(name = "case_status", nullable = false, length = 50)
    @Builder.Default
    private String caseStatus = "OPEN";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_doctor_id")
    private StaffEntity assignedDoctor;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
