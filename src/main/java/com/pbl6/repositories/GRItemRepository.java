package com.pbl6.repositories;

import com.pbl6.entities.GoodsReceiptItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GRItemRepository extends JpaRepository<GoodsReceiptItemEntity,Long> {
    Page<GoodsReceiptItemEntity> findByGoodsReceiptId(Long goodsReceiptId, Pageable pageable);
    void deleteByGoodsReceiptId (Long id);
}
