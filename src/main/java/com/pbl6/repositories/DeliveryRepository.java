package com.pbl6.repositories;

import com.pbl6.entities.DeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryEntity,Long> {
    List<DeliveryEntity> findByOrderId(Long id);
}
