package com.pbl6.repositories;

import com.pbl6.entities.VariantEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface VariantRepository extends JpaRepository<VariantEntity,Long> {
    List<VariantEntity> findByProductId(Long productId);
    Optional<VariantEntity> findByIdAndIsActive(Long productId, boolean active);

    Optional<VariantEntity>  findBySku(String sku);

    @Query("""
        SELECT v FROM VariantEntity v
        JOIN v.product p
        WHERE 
            LOWER(v.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    Page<VariantEntity> searchVariant(@Param("keyword") String keyword, Pageable pageable);
}
