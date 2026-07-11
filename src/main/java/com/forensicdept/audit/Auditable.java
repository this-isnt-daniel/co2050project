package com.forensicdept.audit;

import java.lang.annotation.*;

/**
 * Marker annotation — entities annotated with this will have their lifecycle events
 * captured by {@link AuditEntityListener}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
}
