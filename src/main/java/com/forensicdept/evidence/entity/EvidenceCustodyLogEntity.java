package com.forensicdept.evidence.entity;

import com.forensicdept.staff.entity.StaffEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "evidence_custody_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EvidenceCustodyLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_id", nullable = false)
    private EvidenceEntity evidence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferred_from")
    private StaffEntity transferredFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferred_to")
    private StaffEntity transferredTo;

    @Column(name = "transfer_timestamp", nullable = false)
    private LocalDateTime transferTimestamp;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
