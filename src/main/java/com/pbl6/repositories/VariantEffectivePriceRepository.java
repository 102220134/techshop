package com.pbl6.repositories;

import com.pbl6.entities.VariantEffectivePriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface VariantEffectivePriceRepository extends JpaRepository<VariantEffectivePriceEntity, Long> {

    /**
     * Lấy tất cả variant của 1 product.
     */
    List<VariantEffectivePriceEntity> findByProductId(Long productId);
    Optional<VariantEffectivePriceEntity> findById(Long id);
}
