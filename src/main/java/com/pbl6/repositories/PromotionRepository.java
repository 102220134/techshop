package com.pbl6.repositories;

import com.pbl6.entities.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {

    @Query(
            value = """
        SELECT DISTINCT p.* 
        FROM promotions p
        LEFT JOIN promotion_products pp 
               ON p.id = pp.promotion_id
        WHERE p.status = 'active'
          AND NOW() BETWEEN p.start_date AND p.end_date
          AND (
                p.scope = 'ALL' 
                OR (p.scope = 'PRODUCT' AND pp.product_id = :productId)
              )
        """,
            nativeQuery = true
    )
    List<PromotionEntity> findActiveByProductId(@Param("productId") Long productId);

    @Query("""
    SELECT DISTINCT p FROM PromotionEntity p
    JOIN FETCH p.targets t
    WHERE p.isActive = true
      AND p.startDate <= CURRENT_TIMESTAMP
      AND p.endDate >= CURRENT_TIMESTAMP
      AND (
            t.targetType = 'GLOBAL'
         OR (t.targetType = 'PRODUCT' AND t.targetId IN :productIds)
      )
""")
    List<PromotionEntity> findActivePromotionsForProducts(@Param("productIds") List<Long> productIds);


}
