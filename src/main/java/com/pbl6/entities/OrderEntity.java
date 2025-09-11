package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // buyer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_orders_user"))
    private UserEntity user;

    // salesperson (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id",
            foreignKey = @ForeignKey(name = "fk_orders_sale"))
    private UserEntity sale;

    @Column(nullable=false, length=40)
    private String status = "pending"; // pending, confirmed, ...

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal totalAmount;

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(length=3)
    private String currency = "VND";

    @Column(precision=18, scale=6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    private String shippingMethod;
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id",
            foreignKey = @ForeignKey(name = "fk_orders_voucher"))
    private VoucherEntity voucher;

    // shipping address snapshot
    @Column(nullable=false, length=120)
    private String shipFullName;

    @Column(nullable=false, length=20)
    private String shipPhone;

    @Column(nullable=false, length=200)
    private String shipLine1;

    private String shipLine2;

    @Column(nullable=false, length=100)
    private String shipWard;

    @Column(nullable=false, length=100)
    private String shipDistrict;

    @Column(nullable=false, length=100)
    private String shipProvince;

    private String shipCountry = "VN";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItemEntity> items;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<PaymentEntity> payments;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<DebtEntity> debts;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<VoucherUsageEntity> voucherUsages;
}
