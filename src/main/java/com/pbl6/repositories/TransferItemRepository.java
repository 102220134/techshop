package com.pbl6.repositories;

import com.pbl6.entities.InventoryTransferItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferItemRepository extends JpaRepository<InventoryTransferItemEntity,Long> {
    Page<InventoryTransferItemEntity> findByTransferId(Long id, Pageable pageable);
}
