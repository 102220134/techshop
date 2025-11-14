package com.pbl6.entities;

import com.pbl6.enums.ProductSerialStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "product_serials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSerialEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=100)
    private String serialNo;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="variant_id", nullable=false)
    private VariantEntity variant;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="location_id")
    private InventoryLocationEntity inventoryLocation;

    @Enumerated(EnumType.STRING)
    private ProductSerialStatus status ; // 'in_stock','sold','returned','defective'

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="reservations_id")
    private ReservationEntity reservation;

    @ManyToMany(mappedBy = "productSerials", fetch = FetchType.LAZY)
    private List<InventoryTransferItemEntity> transferItems;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="goods_receipt_item_id")
    private GoodsReceiptItemEntity goodsReceiptItem;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
