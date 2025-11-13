package com.pbl6.repositories;

import com.pbl6.entities.GoodsReceiptEntity;
import com.pbl6.enums.GRStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GRRepository extends JpaRepository<GoodsReceiptEntity,Long> {
    @Query("""
        SELECT gr FROM GoodsReceiptEntity gr
        WHERE (:status IS NULL OR gr.status = :status)
    """)
    Page<GoodsReceiptEntity> findByStatus(
            @Param("status") GRStatus status,
            Pageable pageable
    );
}
