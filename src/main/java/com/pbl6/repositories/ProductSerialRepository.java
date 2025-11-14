package com.pbl6.repositories;

import com.pbl6.entities.InventoryLocationEntity;
import com.pbl6.entities.ProductSerialEntity;
import com.pbl6.enums.ProductSerialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSerialRepository extends JpaRepository<ProductSerialEntity, Long> {
    List<ProductSerialEntity> findByVariantIdAndStatus(Long variantId, ProductSerialStatus status);

    List<ProductSerialEntity> findByVariantIdAndInventoryLocationIdAndStatus(Long variantId, Long id, ProductSerialStatus status);

    List<ProductSerialEntity> findByVariantIdInAndInventoryLocationIdAndStatus(
            Collection<Long> variantIds,
            Long inventoryLocationId,
            ProductSerialStatus status
    );

    boolean existsBySerialNo(String serialNo);

    // FIX: Kiểm tra 1 lần nhiều serial (Batch check) để tối ưu hiệu năng
    @Query("SELECT p.serialNo FROM ProductSerialEntity p WHERE p.serialNo IN :serials")
    List<String> findExistingSerialNos(@Param("serials") List<String> serials);

    // FIX: Hàm update trạng thái hàng loạt cho các serial thuộc 1 chi tiết phiếu nhập
    @Modifying
    @Query("UPDATE ProductSerialEntity p SET p.status = :status WHERE p.goodsReceiptItem.id = :itemId")
    void updateStatusByGRItem(@Param("itemId") Long itemId, @Param("status") ProductSerialStatus status);

    // Tìm tất cả serial hợp lệ trong 1 lần query
    @Query("""
                SELECT ps FROM ProductSerialEntity ps 
                WHERE ps.serialNo IN :serials 
                  AND ps.variant.id = :variantId 
                  AND ps.inventoryLocation.id = :sourceLocationId 
                  AND ps.status = 'IN_STOCK'
            """)
    List<ProductSerialEntity> findAvailableSerialsInLocation(
            @Param("serials") List<String> serials,
            @Param("variantId") Long variantId,
            @Param("sourceLocationId") Long sourceLocationId
    );

    @Modifying
    @Query("""
                UPDATE ProductSerialEntity p 
                SET p.status = 'RESERVED' 
                WHERE p.serialNo IN :serials 
                  AND p.inventoryLocation.id = :locationId 
                  AND p.status = 'IN_STOCK'
            """)
    int reserveSerials(@Param("serials") List<String> serials, @Param("locationId") Long locationId);

    // 1. Dùng cho startTransfer: Chuyển RESERVED -> IN_TRANSFER, Set Location = NULL (Đang đi đường)
    @Modifying
    @Query("""
                UPDATE ProductSerialEntity p 
                SET p.status = 'IN_TRANSFER', 
                    p.inventoryLocation = NULL,
                    p.updatedAt = CURRENT_TIMESTAMP
                WHERE p.serialNo IN :serials 
                  AND p.inventoryLocation.id = :sourceId
                  AND p.status = 'RESERVED'
            """)
    void updateSerialsForShipping(@Param("serials") List<String> serials, @Param("sourceId") Long sourceId);

    // 2. Dùng cho completeTransfer: Chuyển IN_TRANSFER -> AVAILABLE, Set Location = Kho Đích
    @Modifying
    @Query("""
                UPDATE ProductSerialEntity p 
                SET p.status = 'IN_STOCK', 
                    p.inventoryLocation = :destination,
                    p.updatedAt = CURRENT_TIMESTAMP
                WHERE p.serialNo IN :serials 
                  AND p.status = 'IN_TRANSFER'
            """)
    void updateSerialsForReceiving(@Param("serials") List<String> serials, @Param("destination") InventoryLocationEntity destination);


    @Modifying
    @Query("""
                UPDATE ProductSerialEntity s
                SET s.inventoryLocation = :location,
                    s.status = 'RESERVED'
                WHERE s.serialNo IN :serialNos
            """)
    int updateSerialsForStoreReservation(List<String> serialNos, InventoryLocationEntity location);


    @Query("""
                SELECT COUNT(p) FROM ProductSerialEntity p 
                WHERE p.serialNo IN :serials 
                  AND p.inventoryLocation.id = :locationId 
                  AND p.status = 'RESERVED'
            """)
    long countReservedSerials(@Param("serials") List<String> serials, @Param("locationId") Long locationId);
}
