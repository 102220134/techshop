package com.pbl6.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .servers(List.of(new io.swagger.v3.oas.models.servers.Server().url("/")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("AllAPIs") // tên group swagger
                .pathsToMatch("/api/**") // chỉ scan các API /api/admin
                .displayName("All APIs")
                .build();
    }

    // ✅ Group 2: Public/User
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("Public")
                .pathsToMatch("/api/public/**")
                .displayName("Public APIs")
                .build();
    }

    // ✅ Group 2: Public/User
    @Bean
    public GroupedOpenApi userManagerApi() {
        return GroupedOpenApi.builder()
                .group("UserManager")
                .pathsToMatch("/api/private/user/**")
                .displayName("UserManager APIs")
                .build();
    }

    @Bean
    public GroupedOpenApi ProfileApi() {
        return GroupedOpenApi.builder()
                .group("Profile")
                .pathsToMatch("/api/customer/**")
                .displayName("Profile APIs")
                .build();
    }
}
