package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VariantEntity implements Activatable {
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
    private BigDecimal price;


    private Boolean isActive = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // Giá giảm hiệu lực (nếu có bảng variant_effective_price)
    @Formula("(SELECT vep.effective_price FROM variant_effective_price vep WHERE vep.variant_id = id LIMIT 1)")
    private BigDecimal discountedPrice;

    // Tổng tồn kho
    @Formula("(SELECT COALESCE(SUM(i.stock), 0) FROM inventories i WHERE i.variant_id = id)")
    private Integer stock;

    // Tồn kho đã giữ chỗ
    @Formula("(SELECT COALESCE(SUM(i.reserved_stock), 0) FROM inventories i WHERE i.variant_id = id)")
    private Integer reservedStock;

    // Số lượng đã bán
    @Formula("(SELECT COALESCE(SUM(oi.quantity), 0) FROM order_items oi WHERE oi.variant_id = id)")
    private Integer sold;


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

    public int getAvailableStock(){
        return stock-reservedStock;
    }
}
