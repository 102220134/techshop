package com.pbl6.repositories;

import com.pbl6.dtos.response.dashboard.ChartDataPointDTO;
import com.pbl6.entities.OrderEntity;
import com.pbl6.enums.OrderStatus;
import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Page<OrderEntity> findByUserId(Long id, Pageable pageable);

    Page<OrderEntity> findByUserIdAndStatus(Long id, OrderStatus status, Pageable pageable);

    List<OrderEntity> findByStatusAndPaymentMethodAndCreatedAtBefore(OrderStatus orderStatus, PaymentMethod paymentMethod, LocalDateTime timeThreshold);

    @EntityGraph(attributePaths = {"orderItems", "payments"})
    Page<OrderEntity> findAll(Specification<OrderEntity> spec, Pageable pageable);

    //dashboard
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o " +
           "WHERE o.createdAt BETWEEN :start AND :end AND o.status = :status")
    Long sumRevenue(LocalDateTime start, LocalDateTime end, OrderStatus status);

    @Query("SELECT COUNT(o) FROM OrderEntity o " +
           "WHERE o.createdAt BETWEEN :start AND :end")
    Long countOrders(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT " +
                   "    DATE_FORMAT(o.created_at, :format) as label, " +
                   "    SUM(o.total_amount) as revenue, " +
                   "    COUNT(o.id) as orderCount " +
                   "FROM orders o " +
                   "WHERE o.created_at BETWEEN :start AND :end " +
//                   "AND o.status = 'COMPLETED' " +
                   "GROUP BY label " +
                   "ORDER BY label ASC",
            nativeQuery = true)
    List<Object[]> findRevenueChartData(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("format") String format
    );

    @Query("SELECT o.status, COUNT(o) FROM OrderEntity o " +
           "WHERE o.createdAt BETWEEN :start AND :end " +
           "GROUP BY o.status")
    List<Object[]> countOrdersByStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT o.paymentMethod, COUNT(o) FROM OrderEntity o " +
           "WHERE o.createdAt BETWEEN :start AND :end " +
//           "AND o.status = 'DELIVERED' " +
           "GROUP BY o.paymentMethod")
    List<Object[]> countOrdersByPaymentMethod(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o.receiveMethod, COUNT(o) FROM OrderEntity o " +
           "WHERE o.createdAt BETWEEN :start AND :end " +
//           "AND o.status = 'DELIVERED' " +
           "GROUP BY o.receiveMethod")
    List<Object[]> countOrdersByReceiveMethod(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT " +
                   "    CASE WHEN o.is_online = 1 THEN 'Online' ELSE 'Offline' END AS channel, " +
                   "    COUNT(o.id) AS count " +
                   "FROM orders o " +
                   "WHERE o.created_at BETWEEN :start AND :end " +
//                   "AND o.status = 'DELIVERED' " +
                   "GROUP BY channel",
            nativeQuery = true)
    List<Object[]> countOrdersByChannel(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT " +
                   "    u.id AS userId, " +
                   "    u.name AS name, " +
                   "    u.email AS email, " +
                   "    COUNT(o.id) AS totalOrders, " +
                   "    SUM(o.total_amount) AS totalSpent " +
                   "FROM orders o " +
                   "JOIN users u ON o.user_id = u.id " +
                   "WHERE o.created_at BETWEEN :start AND :end " +
//                   "AND u.is_guest = FALSE " +
                   "GROUP BY u.id, u.name, u.email " +
                   "ORDER BY totalSpent DESC " +
                   "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopSpendingCustomers(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("limit") int limit
    );

}
