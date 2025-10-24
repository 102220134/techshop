package com.pbl6.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pbl6.entities.ReviewEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
   Optional<ReviewEntity> findByUserIdAndProductId(Long uid,Long pid);
   Page<ReviewEntity> findByProductId(Long productId, Pageable pageable);
   Page<ReviewEntity> findByProductIdAndRating(Long productId, Short rating,Pageable pageable);
}
