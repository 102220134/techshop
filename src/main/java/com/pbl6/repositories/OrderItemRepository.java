package com.pbl6.repositories;

import com.pbl6.entities.OrderItemEntity;
import com.pbl6.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderItemEntity od " +
           "JOIN od.order o " +
           "WHERE o.createdAt BETWEEN :start AND :end AND o.status = :status")
    Long countProductsSold(LocalDateTime start, LocalDateTime end, OrderStatus status);

    @Query(value = "SELECT " +
                   "    v.product_id AS productId, " +
                   "    oi.product_name AS productName, " +
                   "    SUM(oi.quantity) AS totalSold, " +
                   "    SUM(oi.subtotal) AS totalRevenue, " +
                   "    v.thumbnail AS thumbnail " +
                   "FROM order_items oi " +
                   "JOIN orders o ON oi.order_id = o.id " +
                   "JOIN variants v ON oi.variant_id = v.id " +
                   "WHERE o.created_at BETWEEN :start AND :end " +
//                   "AND o.status = 'COMPLETED' " +
                   "GROUP BY v.product_id, oi.product_name, v.thumbnail " +
                   "ORDER BY totalSold DESC " +
                   "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopSellingProductsNative(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("limit") int limit
    );
}

