package com.pbl6.repositories;

import com.pbl6.entities.GoodsReceiptEntity;
import com.pbl6.entities.InventoryLocationEntity;
import com.pbl6.enums.GRStatus;
import com.pbl6.enums.InventoryLocationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryLocationRepository extends JpaRepository<InventoryLocationEntity,Long>
{
    @Query("""
        SELECT ivl FROM InventoryLocationEntity ivl
        WHERE (:type IS NULL OR ivl.type = :type)
    """)
    List<InventoryLocationEntity> findByType(
            @Param("type") InventoryLocationType type
    );
}
