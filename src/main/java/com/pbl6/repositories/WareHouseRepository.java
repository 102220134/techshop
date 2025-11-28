package com.pbl6.repositories;

import com.pbl6.entities.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface WareHouseRepository extends JpaRepository<WarehouseEntity,Long> {
    Optional<WarehouseEntity> findWarehouseByInventoryLocationId(Long id);

    List<WarehouseEntity> findByInventoryLocationIdIn(Set<Long> warehouseLocIds);
}
