package com.pbl6.repositories;

import com.pbl6.entities.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

    Optional<InventoryEntity> findByInventoryLocationIdAndVariantId(Long locationId,Long variantId);

    List<InventoryEntity> findByVariantId(Long variantId);

    @Query("""
       SELECT COALESCE(SUM(i.stock - i.reservedStock), 0)
       FROM InventoryEntity i
       WHERE i.variant.id = :variantId
       """)
    int getAvailableStockByVariantId(@Param("variantId") Long variantId);

    @Query(value = """
    SELECT * FROM inventory i 
    WHERE i.variant_id = :variantId 
    ORDER BY (i.stock - i.reserved_stock) DESC 
    LIMIT 1
    """, nativeQuery = true)
    Optional<InventoryEntity> findInventoryWithMaxAvailableStock(@Param("variantId") Long variantId);

    @Query("""
    SELECT i FROM InventoryEntity i 
    WHERE i.variant.id = :variantId 
      AND i.inventoryLocation.type = :type
    ORDER BY (i.stock - i.reservedStock) DESC
""")
    Optional<InventoryEntity> findInventoryWithMaxAvailableStockByType(Long variantId, String type);

    List<InventoryEntity> findByVariantIdIn(Collection<Long> variantIds);

}

