package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "debts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DebtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // debtor
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_debts_user"))
    private UserEntity user;

    // related order
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="order_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_debts_order"))
    private OrderEntity order;

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal totalAmount;

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(nullable=false, length=40) // 'unpaid','partial','paid'
    private String status = "unpaid";

    private LocalDateTime dueDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
