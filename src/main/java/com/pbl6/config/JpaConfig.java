package com.pbl6.config;

import com.pbl6.dtos.projection.ProductProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@EnableJpaRepositories(basePackages = "com.pbl6.repositories")
public class JpaConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @PostConstruct
    public void configureResultSetMappings() {
        // This will be handled by the manual mapping in ProductRepositoryImpl
        // No need for @SqlResultSetMapping since we're using manual mapping
    }
}
