package com.forensicdept;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Forensic Medicine Department API.
 * <p>
 * Stack: Spring Boot 3.3 · PostgreSQL 15 · Flyway · Spring Security (JWT) · MapStruct
 */
@SpringBootApplication
@EnableScheduling
public class ForensicDeptApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForensicDeptApplication.class, args);
    }
}
