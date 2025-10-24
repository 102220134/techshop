package com.pbl6.repositories;

import com.pbl6.entities.ReviewMediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewMediaRepository extends JpaRepository<ReviewMediaEntity,Long> {
    List<ReviewMediaEntity> findAllByReviewId(Long reviewId);
}
