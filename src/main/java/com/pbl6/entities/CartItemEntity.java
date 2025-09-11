package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_ci_cart_variant", columnNames = {"user_id","variant_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_id NOT NULL -> users.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ci_cart"))
    private UserEntity user;

    // variant_id NOT NULL -> variants.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ci_variant"))
    private VariantEntity variant;

    @Column(nullable = false)
    private Integer quantity = 1;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
