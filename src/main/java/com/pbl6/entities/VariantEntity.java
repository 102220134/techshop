package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VariantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="product_id", nullable=false)
    private ProductEntity product;

    private String thumbnail;

    @Column(nullable=false, unique=true, length=100)
    private String sku;

    @Column(nullable=false)
    private Double price;

    @Column(length=3)
    private String currency = "VND";

    private Boolean isPublish = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy="variant", fetch=FetchType.LAZY)
    private List<InventoryEntity> inventories;

    @OneToMany(mappedBy="variant", fetch=FetchType.LAZY)
    private List<VariantAttributeValueEntity> variantAttributeValues;

    @OneToMany(mappedBy="variant", fetch=FetchType.LAZY)
    private List<OrderItemEntity> orderItems;

    @OneToMany(mappedBy="variant", fetch=FetchType.LAZY)
    private List<PurchaseOrderItemEntity> purchaseOrderItems;

    @OneToMany(mappedBy="variant", fetch=FetchType.LAZY)
    private List<GoodsReceiptItemEntity> goodsReceiptItems;

    @OneToMany(mappedBy="variant", fetch=FetchType.LAZY)
    private List<ProductSerialEntity> productSerials;
}
