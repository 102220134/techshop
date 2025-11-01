package com.pbl6.entities;

import com.pbl6.enums.InventoryLocationType;
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

    @Enumerated(EnumType.STRING)
    InventoryLocationType type;

    @OneToMany(mappedBy = "inventoryLocation", fetch = FetchType.LAZY)
    private List<GoodsReceiptEntity> goodsReceipts;

    @OneToMany(mappedBy = "inventoryLocation", fetch = FetchType.LAZY)
    private List<ProductSerialEntity> productSerials;
}

