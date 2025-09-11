package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "inventories",
        uniqueConstraints = @UniqueConstraint(name="uk_inventories_variant_warehouse",
                columnNames = {"variant_id","warehouse_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="variant_id", nullable=false)
    private VariantEntity variant;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="warehouse_id", nullable=false)
    private WarehouseEntity warehouse;

    private Integer stock = 0;
    private Integer reservedStock = 0;
    private LocalDateTime updatedAt;
}
