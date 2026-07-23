package com.forensicdept.mlr.entity;

import com.forensicdept.staff.entity.StaffEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Immutable snapshot of an MLR's state captured before each amendment.
 * Revisions are append-only and must never be deleted or overwritten.
 */
@Entity
@Table(name = "mlr_revisions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MlrRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mlr_id", nullable = false)
    private MlrEntity mlr;

    /** Monotonically increasing within a single MLR. Starts at 1. */
    @Column(name = "revision_number", nullable = false)
    private Integer revisionNumber;

    @Column(name = "report_status_at_revision", nullable = false, length = 20)
    private String reportStatusAtRevision;

    @Column(name = "digital_report_path", length = 500)
    private String digitalReportPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revised_by")
    private StaffEntity revisedBy;

    @Column(name = "revision_reason", columnDefinition = "TEXT")
    private String revisionReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
