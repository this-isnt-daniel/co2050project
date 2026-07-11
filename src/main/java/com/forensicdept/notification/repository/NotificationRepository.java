package com.forensicdept.notification.repository;

import com.forensicdept.notification.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    Page<NotificationEntity> findByTargetUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<NotificationEntity> findByTargetUserIdAndNotificationStatusOrderByCreatedAtDesc(
            Long userId, String status, Pageable pageable);

    long countByTargetUserIdAndNotificationStatus(Long userId, String status);
}
