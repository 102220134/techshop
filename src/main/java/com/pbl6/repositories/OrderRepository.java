package com.pbl6.repositories;

import com.pbl6.entities.OrderEntity;
import com.pbl6.enums.OrderStatus;
import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity,Long> {
    List<OrderEntity> findByUserId(Long id);
    List<OrderEntity> findByStatusAndPaymentMethodAndCreatedAtBefore(OrderStatus orderStatus, PaymentMethod paymentMethod,LocalDateTime timeThreshold);
}
