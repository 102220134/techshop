package com.pbl6.repositories;

import com.pbl6.entities.GoodsReceiptEntity;
import com.pbl6.entities.InventoryTransferEntity;
import com.pbl6.enums.GRStatus;
import com.pbl6.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<InventoryTransferEntity,Long> {
    @Query("""
        SELECT it FROM InventoryTransferEntity it
        WHERE (:status IS NULL OR it.status = :status)
    """)
    Page<InventoryTransferEntity> findByStatus(
            @Param("status") TransferStatus status,
            Pageable pageable
    );
}
