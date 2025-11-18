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

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    @EntityGraph(attributePaths = {"transfer"})
    List<ReservationEntity> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"transfer","delivery"})
    List<ReservationEntity> findByIdIn(List<Long> orderIds);

    /**
     * Tìm kiếm Reservation kết hợp filter thông tin từ Order
     */
    @Query("""
                SELECT r FROM ReservationEntity r
                JOIN r.order o
                WHERE (:status IS NULL OR r.status = :status)
                  AND (:receiveMethod IS NULL OR o.receiveMethod = :receiveMethod)
                  AND (:storeId IS NULL OR o.store.id = :storeId)
            """)
    // Tùy chọn: Thêm @EntityGraph nếu bạn muốn fetch luôn thông tin order/items để tránh N+1
    // @EntityGraph(attributePaths = {"order", "order.user"})
    Page<ReservationEntity> searchReservations(
            @Param("status") ReservationStatus status,
            @Param("receiveMethod") ReceiveMethod receiveMethod,
            @Param("storeId") Long storeId,
            Pageable pageable
    );
    List<ReservationEntity> findByTransferId(Long transferId);
    List<ReservationEntity> findByDeliveryId(Long deliveryId);
}
