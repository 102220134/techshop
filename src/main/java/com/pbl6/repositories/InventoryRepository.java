package com.pbl6.repositories;

import com.pbl6.entities.InventoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

    Optional<InventoryEntity> findByInventoryLocationIdAndVariantId(Long locationId, Long variantId);

    List<InventoryEntity> findByVariantId(Long variantId);

    @Query("""
            SELECT DISTINCT i FROM InventoryEntity i
            JOIN i.variant v
            JOIN v.product p
            JOIN p.categories c  
            JOIN i.inventoryLocation il
            WHERE (:locationId IS NULL OR il.id = :locationId)
              AND (:categoryId IS NULL OR c.id = :categoryId)
              AND (
                    COALESCE(:keyword, '') = '' 
                    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(v.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<InventoryEntity> searchInventory(
            @Param("locationId") Long locationId,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );


    List<InventoryEntity> findByVariantIdIn(Collection<Long> variantIds);

}

