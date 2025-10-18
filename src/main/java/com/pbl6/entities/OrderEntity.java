package com.pbl6.entities;

import com.pbl6.enums.OrderStatus;
import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.ReceiveMethod;
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
@Builder
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

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // pending, confirmed, ...

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal totalAmount;

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private ReceiveMethod receiveMethod;
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id",
            foreignKey = @ForeignKey(name = "fk_orders_voucher"))
    private VoucherEntity voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private StoreEntity store;

    // shipping address snapshot
    @Column(nullable=false, length=120)
    private String snapshotName;

    @Column(nullable=false, length=20)
    private String snapshotPhone;

    @Column(nullable=false, length=200)
    private String snapshotLine;

    @Column(nullable=false, length=100)
    private String snapshotWard;

    @Column(nullable=false, length=100)
    private String snapshotDistrict;

    @Column(nullable=false, length=100)
    private String snapshotProvince;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItemEntity> items;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<PaymentEntity> payments;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<DebtEntity> debts;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<VoucherUsageEntity> voucherUsages;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItemEntity> orderItems;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<ReservationEntity> reservations;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getDeliveryAddress(){
        return java.util.stream.Stream.of(snapshotLine, snapshotWard, snapshotDistrict, snapshotProvince)
                .filter(s -> s != null && !s.isBlank())
                .collect(java.util.stream.Collectors.joining(", "));
    }

}
