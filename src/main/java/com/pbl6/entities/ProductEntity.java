package com.pbl6.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity implements Activatable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=200)
    private String name;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private ObjectNode detail;

    @Column(nullable=false, unique=true, length=120)
    private String slug;

    private String thumbnail;
    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<VariantEntity> variants;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "category_products",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<CategoryEntity> categories;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<MediaEntity> medias = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductAttributeValueEntity> productAttributeValues;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ReviewEntity> reviews;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_relations",
            joinColumns = @JoinColumn(name = "product_id"), // cột hiện tại
            inverseJoinColumns = @JoinColumn(name = "related_product_id") // cột liên kết
    )
    private Set<ProductEntity> relatedProducts = new HashSet<>();

    // ✅ Nếu muốn truy ngược lại (đối xứng)
    @ManyToMany(mappedBy = "relatedProducts",fetch = FetchType.LAZY)
    private List<ProductEntity> relatedTo;

    @Formula("(SELECT MIN(v.price) FROM variants v WHERE v.product_id = id AND v.is_active = 1)")
    private BigDecimal price;

    @Formula("(SELECT MIN(vep.effective_price) FROM variant_effective_price vep INNER JOIN variants v ON v.id = vep.variant_id WHERE vep.product_id = id AND v.is_active = 1)")
    private BigDecimal discountedPrice;

    @Formula("(SELECT COALESCE(SUM(i.stock), 0) FROM variants v LEFT JOIN inventories i ON i.variant_id = v.id WHERE v.product_id = id AND v.is_active = 1)")
    private Integer stock;

    @Formula("(SELECT COALESCE(SUM(i.reserved_stock), 0) FROM variants v LEFT JOIN inventories i ON i.variant_id = v.id WHERE v.product_id = id AND v.is_active = 1)")
    private Integer reservedStock;

    @Formula("(SELECT COALESCE(SUM(oi.quantity), 0) FROM variants v LEFT JOIN order_items oi ON oi.variant_id = v.id WHERE v.product_id = id AND v.is_active = 1)")
    private Integer sold;

    @Formula("(SELECT COALESCE(AVG(r.rating), 0) FROM reviews r WHERE r.product_id = id)")
    private Double averageRating;

    @Formula("(SELECT COUNT(*) FROM reviews r WHERE r.product_id = id)")
    private Long totalRating;

    public int getAvailableStock(){
        return stock - reservedStock;
    }
}
