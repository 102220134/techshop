package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "inventory_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransferEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_location_id")
    private InventoryLocationEntity source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_location_id")
    private InventoryLocationEntity destination;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "reservation_transfers", // Tên bảng trung gian chỉ có 2 cột
            joinColumns = @JoinColumn(name = "transfer_id"), // Khóa ngoại của Entity hiện tại (InventoryTransfer)
            inverseJoinColumns = @JoinColumn(name = "reservation_id") // Khóa ngoại của Entity đối diện (StockReservation)
    )
    private Set<ReservationEntity> reservations; // Một chuyển kho có thể liên quan đến NHIỀU đặt chỗ

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
}
