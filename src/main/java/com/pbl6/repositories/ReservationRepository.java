package com.pbl6.repositories;

import com.pbl6.entities.ReservationEntity;
import com.pbl6.enums.ReceiveMethod;
import com.pbl6.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    @EntityGraph(attributePaths = {"transfer"})
    List<ReservationEntity> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"transfer", "delivery"})
    List<ReservationEntity> findByIdIn(List<Long> orderIds);

    /**
     * Tìm kiếm Reservation kết hợp filter thông tin từ Order
     */
    @Query("""
                SELECT r FROM ReservationEntity r
                JOIN r.order o
                WHERE (:status IS NULL OR r.status = :status)
                  AND (:receiveMethod IS NULL OR o.receiveMethod = :receiveMethod)
            """)
    Page<ReservationEntity> searchReservations(
            @Param("status") ReservationStatus status,
            @Param("receiveMethod") ReceiveMethod receiveMethod,
            Pageable pageable
    );

    @Query("""
                SELECT r FROM ReservationEntity r
                JOIN r.order o
                JOIN r.location il
                WHERE (:status IS NULL OR r.status = :status)
                  AND (:receiveMethod IS NULL OR o.receiveMethod = :receiveMethod)
                  AND (:inventoryLocationIds IS NULL OR il.id IN :inventoryLocationIds)
            """)
    Page<ReservationEntity> searchReservations(
            @Param("status") ReservationStatus status,
            @Param("receiveMethod") ReceiveMethod receiveMethod,
            @Param("inventoryLocationIds") Set<Long> inventoryLocationIds,
            Pageable pageable
    );

    List<ReservationEntity> findByTransferId(Long transferId);

    List<ReservationEntity> findByDeliveryId(Long deliveryId);

    Page<ReservationEntity> findByDeliveryId(Long deliveryId, Pageable pageable);
}
