package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=50)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal discountValue;

    @Column(nullable=false, length=40) // 'percentage', 'fixed_amount'
    private String discountType;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime startDate;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime endDate;

    @Column(precision=15, scale=2)
    private BigDecimal minOrderAmount;

    @Column(precision=15, scale=2)
    private BigDecimal maxDiscount;

    private Integer usageLimit;
    private Integer perUserLimit;
    private Integer usedCount = 0;
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "voucher", fetch = FetchType.LAZY)
    private List<OrderEntity> orders;

    @OneToMany(mappedBy = "voucher", fetch = FetchType.LAZY)
    private List<VoucherUsageEntity> usages;
}
