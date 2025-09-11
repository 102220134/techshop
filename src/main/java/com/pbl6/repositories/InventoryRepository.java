package com.pbl6.repositories;

import com.pbl6.dtos.projection.StockProjection;
import com.pbl6.entities.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {
    @Query("""
        SELECT v.product.id as productId, SUM(i.stock) as stock
        FROM InventoryEntity i
        JOIN i.variant v
        WHERE v.product.id IN :ids
        GROUP BY v.product.id
    """)
    List<StockProjection> findStockByProductIds(@Param("ids") List<Long> productIds);
}

