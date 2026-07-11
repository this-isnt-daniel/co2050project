package com.forensicdept.mlef.entity;

import com.forensicdept.audit.AuditEntityListener;
import com.forensicdept.audit.Auditable;
import com.forensicdept.casemanagement.entity.CaseEntity;
import com.forensicdept.staff.entity.StaffEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mlef")
@EntityListeners(AuditEntityListener.class)
@Auditable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MlefEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private CaseEntity caseRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examining_doctor_id", nullable = false)
    private StaffEntity examiningDoctor;

    @Column(name = "date_of_issue")
    private LocalDate dateOfIssue;

    @Column(name = "examination_date_time")
    private LocalDateTime examinationDateTime;

    @Column(name = "nature_of_bodily_harm", columnDefinition = "TEXT")
    private String natureOfBodilyHarm;

    @Column(name = "causative_weapon", length = 255)
    private String causativeWeapon;

    @Column(name = "alcohol_drug_test_results", columnDefinition = "TEXT")
    private String alcoholDrugTestResults;

    @Column(name = "findings", columnDefinition = "TEXT")
    private String findings;

    @Column(name = "report_status", nullable = false, length = 50)
    @Builder.Default
    private String reportStatus = "DRAFT";  // DRAFT | ISSUED | PENDING_COURT_DATE

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
