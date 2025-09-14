package com.pbl6.repositories;

import com.pbl6.entities.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WareHouseRepository extends JpaRepository<WarehouseEntity,Long> {
}
