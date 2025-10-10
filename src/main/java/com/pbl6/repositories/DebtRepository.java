package com.pbl6.repositories;

import com.pbl6.entities.DebtEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DebtRepository extends JpaRepository<DebtEntity,Long> {
    Optional<DebtEntity> findByOrderId(Long orderId);
}
