package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "voucher_usages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherUsageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // voucher used
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="voucher_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_vu_voucher"))
    private VoucherEntity voucher;

    // who used
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_vu_user"))
    private UserEntity user;

    // which order
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="order_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_vu_order"))
    private OrderEntity order;

    private LocalDateTime usedAt;
}
