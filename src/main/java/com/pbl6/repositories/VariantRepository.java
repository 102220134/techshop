package com.pbl6.repositories;

import com.pbl6.entities.VariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface VariantRepository extends JpaRepository<VariantEntity,Long> {
    List<VariantEntity> findByProductId(Long productId);
    Optional<VariantEntity> findByIdAndIsActive(Long productId, boolean active);
}
