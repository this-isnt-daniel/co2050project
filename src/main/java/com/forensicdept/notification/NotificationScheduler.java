package com.forensicdept.notification;

import com.forensicdept.casemanagement.entity.CaseEntity;
import com.forensicdept.config.AppProperties;
import com.forensicdept.courtreport.service.CourtReportService;
import com.forensicdept.mlef.repository.MlefRepository;
import com.forensicdept.mlef.entity.MlefEntity;
import com.forensicdept.notification.entity.NotificationEntity;
import com.forensicdept.notification.service.NotificationService;
import com.forensicdept.postmortem.repository.PostmortemRepository;
import com.forensicdept.postmortem.entity.PostmortemEntity;
import com.forensicdept.courtreport.entity.CourtReportEntity;
import com.forensicdept.courtreport.repository.CourtReportRepository;
import com.forensicdept.user.entity.UserEntity;
import com.forensicdept.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Daily scheduler that:
 * <ol>
 *   <li>Flags MLEF reports still in DRAFT past the configured threshold</li>
 *   <li>Flags postmortem cases where cause of death is not yet finalised</li>
 *   <li>Flags cases with a court date within N days</li>
 * </ol>
 * Results are written to the {@code notifications} table.
 * Email/SMS delivery is out of scope for MVP.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final MlefRepository mlefRepository;
    private final PostmortemRepository postmortemRepository;
    private final CourtReportRepository courtReportRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    @Scheduled(cron = "0 0 7 * * *")  // Daily at 07:00
    public void runDailyChecks() {
        log.info("NotificationScheduler: starting daily checks at {}", LocalDateTime.now());

        checkOverdueMlef();
        checkPendingCauseOfDeath();
        checkUpcomingCourtDates();

        log.info("NotificationScheduler: daily checks complete");
    }

    private void checkOverdueMlef() {
        int threshold = appProperties.getNotification().getPendingReportThresholdDays();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(threshold);
        List<MlefEntity> overdue = mlefRepository.findOverdueDraftMlef(cutoff);

        for (MlefEntity mlef : overdue) {
            CaseEntity caseRef = mlef.getCaseRef();
            UserEntity targetUser = resolveTargetUser(caseRef.getAssignedDoctor() != null
                    ? caseRef.getAssignedDoctor().getId() : null);

            NotificationEntity notification = NotificationEntity.builder()
                    .notificationType("MLEF_PENDING")
                    .relatedCase(caseRef)
                    .targetUser(targetUser)
                    .message(String.format(
                            "MLEF for case %s has been in DRAFT status for more than %d days. Please issue the report.",
                            caseRef.getCaseNumber(), threshold))
                    .notificationStatus("UNREAD")
                    .build();
            notificationService.saveNotification(notification);
            log.info("Created MLEF_PENDING notification for case {}", caseRef.getCaseNumber());
        }
    }

    private void checkPendingCauseOfDeath() {
        int threshold = appProperties.getNotification().getPendingReportThresholdDays();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(threshold);
        List<PostmortemEntity> pending = postmortemRepository.findPendingCauseOfDeath(cutoff);

        for (PostmortemEntity pm : pending) {
            CaseEntity caseRef = pm.getCaseRef();
            UserEntity targetUser = resolveTargetUser(pm.getDoctor() != null ? pm.getDoctor().getId() : null);

            NotificationEntity notification = NotificationEntity.builder()
                    .notificationType("COD_PENDING")
                    .relatedCase(caseRef)
                    .targetUser(targetUser)
                    .message(String.format(
                            "Cause of death for autopsy case %s has not been finalised. Please update the postmortem report.",
                            caseRef.getCaseNumber()))
                    .notificationStatus("UNREAD")
                    .build();
            notificationService.saveNotification(notification);
            log.info("Created COD_PENDING notification for case {}", caseRef.getCaseNumber());
        }
    }

    private void checkUpcomingCourtDates() {
        int daysAhead = appProperties.getNotification().getCourtDateUpcomingDays();
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(daysAhead);
        List<CourtReportEntity> upcoming = courtReportRepository.findUpcomingTrials(today, cutoff);

        for (CourtReportEntity report : upcoming) {
            CaseEntity caseRef = report.getCaseRef();
            UserEntity targetUser = resolveTargetUser(caseRef.getAssignedDoctor() != null
                    ? caseRef.getAssignedDoctor().getId() : null);

            NotificationEntity notification = NotificationEntity.builder()
                    .notificationType("COURT_DATE_UPCOMING")
                    .relatedCase(caseRef)
                    .targetUser(targetUser)
                    .message(String.format(
                            "Court date for case %s at %s is on %s (within %d days). Please prepare.",
                            caseRef.getCaseNumber(), report.getCourtName(), report.getDateOfTrial(), daysAhead))
                    .notificationStatus("UNREAD")
                    .build();
            notificationService.saveNotification(notification);
            log.info("Created COURT_DATE_UPCOMING notification for case {}", caseRef.getCaseNumber());
        }
    }

    /** Resolves the admin user as fallback when no doctor is assigned. */
    private UserEntity resolveTargetUser(Long staffId) {
        if (staffId != null) {
            return userRepository.findAll().stream()
                    .filter(u -> u.getStaff() != null && u.getStaff().getId().equals(staffId))
                    .findFirst().orElse(null);
        }
        // Fallback: notify admin
        return userRepository.findByUsername("admin").orElse(null);
    }
}
