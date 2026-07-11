package com.forensicdept.notification.entity;

import com.forensicdept.casemanagement.entity.CaseEntity;
import com.forensicdept.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;  // MLEF_PENDING | COD_PENDING | COURT_DATE_UPCOMING

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_case_id")
    private CaseEntity relatedCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private UserEntity targetUser;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "notification_status", nullable = false, length = 20)
    @Builder.Default
    private String notificationStatus = "UNREAD";  // UNREAD | READ | SENT

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
