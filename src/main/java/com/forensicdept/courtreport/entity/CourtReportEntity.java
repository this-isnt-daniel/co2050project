package com.forensicdept.courtreport.entity;

import com.forensicdept.audit.AuditEntityListener;
import com.forensicdept.audit.Auditable;
import com.forensicdept.casemanagement.entity.CaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "court_reports")
@EntityListeners(AuditEntityListener.class)
@Auditable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourtReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private CaseEntity caseRef;

    @Column(name = "report_type", nullable = false, length = 20)
    private String reportType;  // MLR | PMR

    @Column(name = "submission_date")
    private LocalDate submissionDate;

    @Column(name = "report_status", nullable = false, length = 50)
    @Builder.Default
    private String reportStatus = "DRAFT";  // DRAFT | ISSUED | PENDING_COURT_DATE

    @Column(name = "court_name", length = 255)
    private String courtName;

    @Column(name = "date_of_trial")
    private LocalDate dateOfTrial;

    @Column(name = "certificate_of_receipt_ref", length = 255)
    private String certificateOfReceiptRef;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
