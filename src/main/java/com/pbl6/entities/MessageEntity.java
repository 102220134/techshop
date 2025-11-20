package com.pbl6.entities;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ROOM -> MESSAGES (1 - n)
    @Column(nullable = false)
    private Long roomId;

    // USER / SYSTEM
    @Column(nullable = false, length = 20)
    private String senderType;

    // USER_xxx hoặc SYSTEM hoặc STAFF_xxx
    @Column(nullable = false, length = 100)
    private String senderKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // text, image, file, system-notification…
    private String type = "text";

    private LocalDateTime createdAt = LocalDateTime.now();
}
