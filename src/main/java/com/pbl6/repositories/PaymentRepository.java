package com.pbl6.repositories;

import com.pbl6.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity,Long> {
    Optional<PaymentEntity> findByTransactionRef(String transactionRef);
    Optional<PaymentEntity> findTopByOrderIdOrderByIdDesc(Long orderId);
}
