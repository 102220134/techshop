package com.pbl6.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // USER_5 hoặc GUEST_uuid
    @Column(nullable = false, length = 100, unique = true)
    private String userKey;

    // Nhân viên nào đang claim phòng (nếu bạn dùng tính năng claim)
    // Nếu không dùng thì để null
    private String claimedBy; // STAFF_1, STAFF_2 ...

    // Last message preview để staff UI load nhanh
    @Column(length = 500)
    private String lastMessage;

    // Timestamp để staff sort theo phòng mới nhất
    private LocalDateTime lastMessageTime;

    // Số tin nhắn user chưa đọc (để staff biết phòng nào có người nhắn)
    private Integer unreadCount = 0;

    // active / closed
    private String status = "active";

    // Tự động set thời gian tạo
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Auto update khi có thay đổi
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

