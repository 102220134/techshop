package com.pbl6.repositories;

import com.pbl6.dtos.projection.SoldProjection;
import com.pbl6.entities.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    @Query("""
        SELECT v.product.id as productId, SUM(oi.quantity) as sold
        FROM OrderItemEntity oi
        JOIN oi.variant v
        WHERE v.product.id IN :ids
        GROUP BY v.product.id
    """)
    List<SoldProjection> findSoldByProductIds(@Param("ids") List<Long> productIds);
}

