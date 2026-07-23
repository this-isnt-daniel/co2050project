package com.forensicdept.evidence.entity;

import com.forensicdept.audit.AuditEntityListener;
import com.forensicdept.audit.Auditable;
import com.forensicdept.casemanagement.entity.CaseEntity;
import com.forensicdept.staff.entity.StaffEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evidence")
@EntityListeners(AuditEntityListener.class)
@Auditable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EvidenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evidence_number", unique = true, length = 30)
    private String evidenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private CaseEntity caseRef;

    @Column(name = "evidence_type", nullable = false, length = 100)
    private String evidenceType;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "storage_location", length = 255)
    private String storageLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collected_by")
    private StaffEntity collectedBy;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @OneToMany(mappedBy = "evidence", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EvidenceCustodyLogEntity> custodyLog = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
