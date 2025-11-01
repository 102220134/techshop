package com.pbl6.repositories;

import com.pbl6.entities.OrderEntity;
import com.pbl6.enums.OrderStatus;
import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity,Long> {
    Page<OrderEntity> findByUserId(Long id, Pageable pageable);
    Page<OrderEntity> findByUserIdAndStatus(Long id, OrderStatus status,Pageable pageable);
    List<OrderEntity> findByStatusAndPaymentMethodAndCreatedAtBefore(OrderStatus orderStatus, PaymentMethod paymentMethod,LocalDateTime timeThreshold);

    @EntityGraph(attributePaths = {"orderItems","payments"})
    Page<OrderEntity> findAll(Specification<OrderEntity> spec, Pageable pageable);
}
