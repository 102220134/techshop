package com.pbl6.repositories;

import com.pbl6.entities.InventoryTransferItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryTransferItemRepository extends JpaRepository<InventoryTransferItemEntity,Long> {
}
