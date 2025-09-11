package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=50)
    private String code;

    @Column(nullable=false, length=100)
    private String name;

    private String address;
    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
    private List<InventoryEntity> inventories;

    @OneToMany(mappedBy = "fromWarehouse", fetch = FetchType.LAZY)
    private List<StockMovementEntity> outMovements;

    @OneToMany(mappedBy = "toWarehouse", fetch = FetchType.LAZY)
    private List<StockMovementEntity> inMovements;

    @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
    private List<GoodsReceiptEntity> goodsReceipts;

    @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
    private List<ProductSerialEntity> productSerials;
}
