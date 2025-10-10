package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "inventory_locations")
public class InventoryLocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;  // WAREHOUSE, STORE

    @OneToMany(mappedBy = "inventoryLocation", fetch = FetchType.LAZY)
    private List<GoodsReceiptEntity> goodsReceipts;

    @OneToMany(mappedBy = "inventoryLocation", fetch = FetchType.LAZY)
    private List<ProductSerialEntity> productSerials;
}

