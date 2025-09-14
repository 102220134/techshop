package com.pbl6.repositories;

import com.pbl6.entities.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {
    Optional<InventoryEntity> findByWarehouseIdAndVariantId(Long warehouseId,Long variantId);
    List<InventoryEntity> findByVariantId(Long variantId);
}

