package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="supplier_id", nullable=false)
    private SupplierEntity supplier;

    @Column(nullable=false, length=40)
    private String status = "draft"; // 'draft','pending','approved','received','cancelled'

    private LocalDateTime expectedDate;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy="purchaseOrder", fetch=FetchType.LAZY)
    private List<PurchaseOrderItemEntity> items;

    @OneToMany(mappedBy="purchaseOrder", fetch=FetchType.LAZY)
    private List<GoodsReceiptEntity> goodsReceipts;
}
