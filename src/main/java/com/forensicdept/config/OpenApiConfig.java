package com.forensicdept.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures OpenAPI / Swagger UI with JWT bearer authentication support.
 * Access at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI forensicDeptOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Forensic Medicine Department API")
                        .description("RESTful backend for the Forensic Medicine Department Database System. "
                                + "Handles clinical examinations, autopsies, evidence chain-of-custody, "
                                + "court reports, audit trails, and RBAC for medico-legal staff.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Forensic Department IT")
                                .email("it@forensicdept.gov.lk")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT access token below (obtained from POST /api/auth/login)")));
    }
}
