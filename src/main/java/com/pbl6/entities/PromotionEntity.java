package com.pbl6.entities;

import com.pbl6.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "promotions")
public class PromotionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private BigDecimal discountValue;
    private BigDecimal maxDiscountValue;

    private Integer priority;
    private Boolean exclusive = false;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private boolean isActive;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PromotionTargetEntity> targets;

    @ManyToMany(mappedBy = "promotions")
    private List<OrderItemEntity> orderItems = new ArrayList<>();

}

