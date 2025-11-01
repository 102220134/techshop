package com.pbl6.repositories;

import com.pbl6.entities.ReservationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity,Long> {
    @EntityGraph(attributePaths = {"transfer"})
    List<ReservationEntity> findByOrderId(Long orderId);
}
