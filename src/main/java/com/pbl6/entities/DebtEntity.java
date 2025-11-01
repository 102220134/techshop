package com.pbl6.entities;

import com.pbl6.enums.DebtStatus;
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

    @Enumerated(EnumType.STRING)
    private DebtStatus status;

    private LocalDateTime dueDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
