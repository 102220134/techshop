package com.pbl6.entities;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho view variant_effective_price.
 * View này lưu giá cuối cùng sau khi áp dụng khuyến mãi.
 */
@Entity
@Table(name = "variant_effective_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Immutable
public class VariantEffectivePriceEntity {

    @Id
    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "effective_price")
    private BigDecimal effectivePrice;
}
