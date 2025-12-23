package com.pbl6.repositories;

import com.pbl6.entities.DeliveryEntity;
import com.pbl6.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Set;

@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long> {
    List<DeliveryEntity> findByOrderId(Long id);

    @Query(
            value = """
                        SELECT DISTINCT d
                        FROM DeliveryEntity d
                        JOIN ReservationEntity r ON r.delivery = d
                        JOIN r.location l
  WHERE (:status IS NULL OR d.status = :status)
                    """,
            countQuery = """
                        SELECT COUNT(DISTINCT d.id)
                        FROM DeliveryEntity d
                        JOIN ReservationEntity r ON r.delivery = d
                        JOIN r.location l
WHERE (:status IS NULL OR d.status = :status)
                    """
    )
    Page<DeliveryEntity> findByStatus(
            @Param("status") DeliveryStatus status,
            Pageable pageable
    );

    @Query(
            value = """
                        SELECT DISTINCT d
                        FROM DeliveryEntity d
                        JOIN ReservationEntity r ON r.delivery = d
                        JOIN r.location l
                        WHERE (:status IS NULL OR d.status = :status)
                        AND l.id IN :locationIds
                    """,
            countQuery = """
                        SELECT COUNT(DISTINCT d.id)
                        FROM DeliveryEntity d
                        JOIN ReservationEntity r ON r.delivery = d
                        JOIN r.location l
                        WHERE (:status IS NULL OR d.status = :status)
                        AND l.id IN :locationIds
                    """
    )
    Page<DeliveryEntity> findByStatusNullableAndLocations(
            @Param("status") DeliveryStatus status,
            @Param("locationIds") Set<Long> locationIds,
            Pageable pageable
    );


}
