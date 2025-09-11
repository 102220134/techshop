package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

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
    @JoinColumn(name="warehouse_id")
    private WarehouseEntity warehouse;

    @Column(nullable=false, length=40)
    private String status = "in_stock"; // 'in_stock','sold','returned','defective'

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="order_item_id")
    private OrderItemEntity orderItem;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
