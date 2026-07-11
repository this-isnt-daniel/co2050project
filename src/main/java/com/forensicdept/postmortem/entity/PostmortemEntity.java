package com.forensicdept.postmortem.entity;

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
@Table(name = "postmortem")
@EntityListeners(AuditEntityListener.class)
@Auditable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostmortemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private CaseEntity caseRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private StaffEntity doctor;

    @Column(name = "inquest_order_ref", length = 255)
    private String inquestOrderRef;

    @Column(name = "inquest_date")
    private LocalDate inquestDate;

    @Column(name = "place_of_pm", length = 255)
    private String placeOfPm;

    @Column(name = "cause_of_death_category", length = 50)
    private String causeOfDeathCategory;  // NATURAL | ACCIDENTAL | SUICIDAL | HOMICIDAL

    @Column(name = "findings", columnDefinition = "TEXT")
    private String findings;

    @Column(name = "cause_of_death", columnDefinition = "TEXT")
    private String causeOfDeath;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
