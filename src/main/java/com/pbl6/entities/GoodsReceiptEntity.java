package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "goods_receipts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="purchase_order_id")
    private PurchaseOrderEntity purchaseOrder;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="warehouse_id", nullable=false)
    private WarehouseEntity warehouse;

    @Column(nullable=false)
    private LocalDateTime receiptDate;

    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy="goodsReceipt", fetch=FetchType.LAZY)
    private List<GoodsReceiptItemEntity> items;
}
