package com.pbl6.entities;

import com.pbl6.enums.TargetType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "promotion_targets")
public class PromotionTargetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private PromotionEntity promotion;

    @Enumerated(EnumType.STRING)
    private TargetType targetType; // PRODUCT, CATEGORY, BRAND, GLOBAL

    private Long targetId; // null nếu GLOBAL
}
