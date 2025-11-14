package com.pbl6.repositories;

import com.pbl6.entities.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovementEntity, Long> {
}

