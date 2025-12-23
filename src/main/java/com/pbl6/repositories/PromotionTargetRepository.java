package com.pbl6.repositories;

import com.pbl6.entities.PromotionTargetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionTargetRepository extends JpaRepository<PromotionTargetEntity, Long> {
    void deleteByPromotionId(Long promotionId);
}
