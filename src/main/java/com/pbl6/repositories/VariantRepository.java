package com.pbl6.repositories;

import com.pbl6.entities.VariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VariantRepository extends JpaRepository<VariantEntity,Long> {
    List<VariantEntity> findByProductId(Long productId);
    Optional<VariantEntity> findByIdAndIsActive(Long productId, boolean active);
}
