package com.pbl6.entities;

import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(name="uk_payments_txref", columnNames = "transaction_ref")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // parent order
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="order_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_pay_order"))
    private OrderEntity order;

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(length=60)
    private String provider;

    @Column(name="transaction_ref", length=120)
    private String transactionRef;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
