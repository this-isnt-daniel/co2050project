package com.forensicdept.mlr.entity;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Medico-Legal Report (MLR) — the official report prepared by the JMO
 * after examining the patient. Distinct from the MLEF (referral) and
 * from the Court Report (court submission).
 *
 * <p>One active MLR per Case. Before any update, the current state is
 * snapshotted as an {@link MlrRevisionEntity} to preserve audit history.
 * Once finalized, the MLR cannot be directly edited — a new revision must
 * be raised by an ADMIN or JMO.</p>
 */
@Entity
@Table(name = "mlr")
@EntityListeners(AuditEntityListener.class)
@Auditable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MlrEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Official MLR serial number, e.g. MLR/2026/000001.
     * Assigned once on creation — never changed afterwards.
     */
    @Column(name = "mlr_number", nullable = false, unique = true, length = 30)
    private String mlrNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private CaseEntity caseRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prepared_by", nullable = false)
    private StaffEntity preparedBy;

    @Column(name = "examination_date")
    private LocalDate examinationDate;

    @Column(name = "date_finalized")
    private LocalDate dateFinalized;

    /**
     * DRAFT → FINALIZED (one-way transition).
     * A finalized MLR must not be edited; raise a revision instead.
     */
    @Column(name = "report_status", nullable = false, length = 20)
    @Builder.Default
    private String reportStatus = "DRAFT";  // DRAFT | FINALIZED

    /** Path/reference to the digital report file stored via the DOCUMENTS table. */
    @Column(name = "digital_report_path", length = 500)
    private String digitalReportPath;

    @OneToMany(mappedBy = "mlr", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<MlrRevisionEntity> revisions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
