package com.pbl6.entities;

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

    @Column(nullable=false, length=40) // 'cash','bank_transfer',...
    private String method;

    @Column(nullable=false, length=40) // 'pending','completed',...
    private String status = "pending";

    @Column(length=60)
    private String provider;

    @Column(name="transaction_ref", length=120)
    private String transactionRef;

    @Column(length=3)
    private String currency = "VND";

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
