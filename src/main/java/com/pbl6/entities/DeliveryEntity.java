package com.pbl6.entities;

import com.pbl6.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="order_id", nullable=false)
    private OrderEntity order;

    // Tên hãng vận chuyển (GHTK, GHN...) - Nhập tay
    @Column(name = "carrier_name", nullable = false, length = 100)
    private String carrierName;

    // Mã vận đơn - Nhập tay từ App hãng
    @Column(name = "tracking_code", nullable = false, unique = true, length = 50)
    private String trackingCode;

    // Phí ship trả cho hãng
    @Column(name = "shipping_fee", precision = 15, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    // Tiền thu hộ
    @Column(name = "cod_amount", precision = 15, scale = 2)
    private BigDecimal codAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = DeliveryStatus.PENDING;
        }
        // Nếu phí ship null thì set bằng 0
        if (this.shippingFee == null) this.shippingFee = BigDecimal.ZERO;
        if (this.codAmount == null) this.codAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
