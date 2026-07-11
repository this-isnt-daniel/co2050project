package com.forensicdept.notification.service;

import com.forensicdept.exception.ResourceNotFoundException;
import com.forensicdept.notification.dto.NotificationResponse;
import com.forensicdept.notification.entity.NotificationEntity;
import com.forensicdept.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','LAB_STAFF','CLERICAL')")
    @Transactional(readOnly = true)
    public Page<NotificationResponse> findForUser(Long userId, String status, Pageable pageable) {
        if (status != null) {
            return notificationRepository
                    .findByTargetUserIdAndNotificationStatusOrderByCreatedAtDesc(userId, status, pageable)
                    .map(this::toResponse);
        }
        return notificationRepository.findByTargetUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','LAB_STAFF','CLERICAL')")
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByTargetUserIdAndNotificationStatus(userId, "UNREAD");
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','LAB_STAFF','CLERICAL')")
    @Transactional
    public NotificationResponse markRead(Long id) {
        NotificationEntity notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        notification.setNotificationStatus("READ");
        return toResponse(notificationRepository.save(notification));
    }

    /** Called internally by the {@link com.forensicdept.notification.NotificationScheduler}. */
    @Transactional
    public void saveNotification(NotificationEntity notification) {
        notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(NotificationEntity e) {
        return NotificationResponse.builder()
                .id(e.getId())
                .notificationType(e.getNotificationType())
                .relatedCaseId(e.getRelatedCase() != null ? e.getRelatedCase().getId() : null)
                .relatedCaseNumber(e.getRelatedCase() != null ? e.getRelatedCase().getCaseNumber() : null)
                .targetUserId(e.getTargetUser() != null ? e.getTargetUser().getId() : null)
                .message(e.getMessage())
                .notificationStatus(e.getNotificationStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
