package com.pbl6.repositories;

import com.pbl6.entities.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<StoreEntity,Long> {
    Optional<StoreEntity> findByInventoryLocationId(Long id);
}
