package com.pbl6.repositories;

import com.pbl6.entities.DebtEntity;
import com.pbl6.enums.DebtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DebtRepository extends JpaRepository<DebtEntity,Long> {
    Optional<DebtEntity> findByOrderId(Long orderId);
//    Optional<DebtEntity> findByOrderIdAndStatus(Long orderId, DebtStatus status);
}
