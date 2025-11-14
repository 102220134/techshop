package com.pbl6.entities;

import com.pbl6.enums.GRStatus;
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
    @JoinColumn(name="supplier_id")
    private SupplierEntity supplier;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="location_id", nullable=false)
    private InventoryLocationEntity inventoryLocation;

    @Column(nullable=false)
    private LocalDateTime receiptDate;

    @Enumerated(EnumType.STRING)
    private GRStatus status;

    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy="goodsReceipt", fetch=FetchType.LAZY)
    private List<GoodsReceiptItemEntity> items;
}
