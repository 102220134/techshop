package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // parent order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_oi_order"))
    private OrderEntity order;

    // selected variant (RESTRICT in DB)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="variant_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_oi_variant"))
    private VariantEntity variant;

    @Column(nullable=false, length=200)
    private String productName;

    // JSON attributes snapshot
    @Column(columnDefinition = "JSON")
    private String variantAttributes;

    @Column(nullable=false, length=100)
    private String sku;

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal price;

    @Column(nullable=false)
    private Integer quantity;

    @Column(precision=15, scale=2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // reverse link for serials
    @OneToMany(mappedBy = "orderItem", fetch = FetchType.LAZY)
    private List<ProductSerialEntity> productSerials;

    // reviews unique by order_item_id (see ReviewEntity)
    @OneToMany(mappedBy = "orderItem", fetch = FetchType.LAZY)
    private List<ReviewEntity> reviews;
}
