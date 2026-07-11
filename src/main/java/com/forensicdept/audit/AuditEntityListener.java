package com.forensicdept.audit;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA entity listener that writes to {@code audit_logs} on every CREATE / UPDATE / DELETE
 * of any entity annotated with {@link Auditable}.
 *
 * <p>Spring beans cannot be injected directly into entity listeners; we use
 * {@link ApplicationContext} via a static holder pattern.
 */
@Slf4j
@Component
public class AuditEntityListener {

    private static ApplicationContext applicationContext;

    /** Called by Spring to inject the context into the static holder. */
    @Autowired
    public void setApplicationContext(ApplicationContext ctx) {
        AuditEntityListener.applicationContext = ctx;
    }

    @PostPersist
    public void onPostPersist(Object entity) {
        writeAudit(entity, "CREATE");
    }

    @PostUpdate
    public void onPostUpdate(Object entity) {
        writeAudit(entity, "UPDATE");
    }

    @PostRemove
    public void onPostRemove(Object entity) {
        writeAudit(entity, "DELETE");
    }

    private void writeAudit(Object entity, String action) {
        if (applicationContext == null) {
            log.warn("AuditEntityListener: applicationContext not initialised — skipping audit for {}", action);
            return;
        }
        try {
            AuditLogRepository repo = applicationContext.getBean(AuditLogRepository.class);
            AuditContext ctx = applicationContext.getBean(AuditContext.class);

            Long entityId = extractId(entity);
            String entityName = entity.getClass().getSimpleName();
            Map<String, Object> summary = buildSummary(entity);

            AuditLog entry = AuditLog.builder()
                    .entityName(entityName)
                    .entityId(entityId)
                    .action(action)
                    .performedBy(ctx.currentUsername())
                    .performedAt(LocalDateTime.now())
                    .changeSummary(summary)
                    .build();

            repo.save(entry);
        } catch (Exception e) {
            // Never let audit failures break the main transaction
            log.error("Failed to write audit log for {} on {}: {}", action, entity.getClass().getSimpleName(), e.getMessage());
        }
    }

    private Long extractId(Object entity) {
        try {
            for (Field f : entity.getClass().getDeclaredFields()) {
                if (f.isAnnotationPresent(Id.class)) {
                    f.setAccessible(true);
                    Object val = f.get(entity);
                    return val instanceof Long ? (Long) val : null;
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract id from {}", entity.getClass().getSimpleName());
        }
        return null;
    }

    private Map<String, Object> buildSummary(Object entity) {
        Map<String, Object> summary = new HashMap<>();
        for (Field f : entity.getClass().getDeclaredFields()) {
            try {
                f.setAccessible(true);
                Object value = f.get(entity);
                if (value != null && !f.getName().equals("passwordHash")) {
                    summary.put(f.getName(), value.toString());
                }
            } catch (Exception ignored) { }
        }
        return summary;
    }
}
