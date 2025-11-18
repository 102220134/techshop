package com.pbl6.entities;
import com.pbl6.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="order_id")
    private OrderEntity order;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="order_item_id")
    private OrderItemEntity orderItem;

    @Column(nullable = false)
    private Integer quantity = 1;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="location_id")
    private InventoryLocationEntity location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.DRAFT;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Quan hệ với ProductSerial ---
    @OneToMany(mappedBy = "reservation",fetch = FetchType.LAZY)
    private List<ProductSerialEntity> productSerials;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="transfer_id")
    private InventoryTransferEntity transfer;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="delivery_id")
    private DeliveryEntity delivery;

    // getters và setters
    // ...
}
