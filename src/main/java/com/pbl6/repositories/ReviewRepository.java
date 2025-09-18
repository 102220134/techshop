package com.pbl6.repositories;

import com.pbl6.dtos.projection.ReviewStatProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pbl6.entities.ReviewEntity;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    @Query(value = """
    SELECT 
        COUNT(*) as total,
        COALESCE(AVG(r.rating), 0) as average,
        COUNT(CASE WHEN r.rating = 1 THEN 1 END) as star1,
        COUNT(CASE WHEN r.rating = 2 THEN 1 END) as star2,
        COUNT(CASE WHEN r.rating = 3 THEN 1 END) as star3,
        COUNT(CASE WHEN r.rating = 4 THEN 1 END) as star4,
        COUNT(CASE WHEN r.rating = 5 THEN 1 END) as star5
    FROM reviews r
    WHERE r.product_id = :productId
    """, nativeQuery = true)
    Optional<ReviewStatProjection> getReviewStats(@Param("productId") Long productId);

}
