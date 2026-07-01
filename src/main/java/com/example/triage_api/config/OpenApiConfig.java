package com.example.triage_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Triage API")
                        .description("""
                                ## AI-Powered Symptom Triage REST API

                                A healthcare backend that lets patients report symptoms and receive \
                                an AI-generated triage assessment, then connect with available doctors \
                                for a private consultation.

                                ### Typical patient flow
                                1. `POST /api/auth/register` — create a patient account
                                2. `POST /api/auth/login` — get a JWT token
                                3. `POST /api/sessions` — open a triage session
                                4. `POST /api/sessions/{id}/entries` — add one or more symptoms
                                5. `POST /api/sessions/{id}/triage` — run AI analysis (GREEN / AMBER / RED)
                                6. `GET  /api/doctors/available` — browse available doctors
                                7. `POST /api/conversations` — request a consultation
                                8. `POST /api/conversations/{id}/messages` — chat with the doctor

                                ### Authentication
                                All endpoints except `/api/auth/**`, `/api/doctors` (GET), \
                                `/api/doctors/available` (GET), `/api/doctors/{id}` (GET), \
                                and the Swagger UI paths require a **Bearer JWT token** in the \
                                `Authorization` header.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Eunice Ngowo")
                                .email("eunice@ngowo.dev"))
                        .license(new License()
                                .name("Proprietary")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local development"),
                        new Server().url("https://api.triage.example.com").description("Production")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste the JWT token from POST /api/auth/login. Example: `eyJhbGci...`")));
    }
}
