package com.pbl6.repositories;

import com.pbl6.entities.InventoryTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<InventoryTransferEntity,Long> {

}
