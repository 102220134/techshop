package com.pbl6.repositories;

import com.pbl6.dtos.response.inventory.movement.StockMovementDto;
import com.pbl6.entities.StockMovementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovementEntity, Long> {
    @Query("""
        SELECT s FROM StockMovementEntity s
        LEFT JOIN FETCH s.variant v
        LEFT JOIN FETCH s.inventoryLocation l
        WHERE (:variantId IS NULL OR v.id = :variantId)
        AND (:locationId IS NULL OR l.id = :locationId)
    """)
    Page<StockMovementEntity> findByVariantIdAndLocationId(
            @Param("variantId") Long variantId,
            @Param("locationId") Long locationId,
            Pageable pageable
    );
}

