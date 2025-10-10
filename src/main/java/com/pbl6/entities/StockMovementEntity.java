package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="variant_id", nullable=false)
    private VariantEntity variant;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="location_id")
    private InventoryLocationEntity inventoryLocation;

    private Integer quantityDelta;

    @Column(nullable=false, length=50)
    private String reason; // draft from DDL: 'transfer','sale','return',...

    private String refType;
    private Long refId;

    private LocalDateTime createdAt;
}
