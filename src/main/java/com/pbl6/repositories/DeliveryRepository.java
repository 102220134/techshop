package com.pbl6.repositories;

import com.pbl6.entities.DeliveryEntity;
import com.pbl6.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryEntity,Long> {
    List<DeliveryEntity> findByOrderId(Long id);

    Page<DeliveryEntity> findByStatus(DeliveryStatus status, PageRequest pageable);
}
