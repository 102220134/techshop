package com.pbl6.repositories;

import com.pbl6.entities.ProductSerialEntity;
import com.pbl6.enums.ProductSerialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductSerialRepository extends JpaRepository<ProductSerialEntity,Long> {
    List<ProductSerialEntity> findByVariantIdAndStatus(Long variantId, ProductSerialStatus status);
    List<ProductSerialEntity> findByVariantIdAndInventoryLocationIdAndStatus(Long variantId, Long id , ProductSerialStatus status);
    List<ProductSerialEntity> findByVariantIdInAndInventoryLocationIdAndStatus(
            Collection<Long> variantIds,
            Long inventoryLocationId,
            ProductSerialStatus status
    );

}
