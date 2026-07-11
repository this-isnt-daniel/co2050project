package com.forensicdept.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Typed binding for the {@code app.*} configuration block in application.yml.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Storage storage = new Storage();
    private Notification notification = new Notification();
    private Cors cors = new Cors();

    @Data
    public static class Jwt {
        private String secret;
        private long expirationMs = 86_400_000L; // 24 hours
    }

    @Data
    public static class Storage {
        private String documentBasePath = "/data/documents";
    }

    @Data
    public static class Notification {
        private int pendingReportThresholdDays = 7;
        private int courtDateUpcomingDays = 14;
    }

    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:3000";
    }
}
