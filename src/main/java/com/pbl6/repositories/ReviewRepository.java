package com.pbl6.repositories;

import com.pbl6.dtos.projection.RatingProjection;
import com.pbl6.entities.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    @Query("""
        SELECT r.product.id as productId,
               COUNT(r) as total,
               SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END) as star1,
               SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END) as star2,
               SUM(CASE WHEN r.rating = 3 THEN 1 ELSE 0 END) as star3,
               SUM(CASE WHEN r.rating = 4 THEN 1 ELSE 0 END) as star4,
               SUM(CASE WHEN r.rating = 5 THEN 1 ELSE 0 END) as star5,
               AVG(r.rating) as average
        FROM ReviewEntity r
        GROUP BY r.product.id
    """)
    List<RatingProjection> findRatingByProductIds(@Param("ids") List<Long> productIds);
}

