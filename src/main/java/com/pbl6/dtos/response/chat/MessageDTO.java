package com.pbl6.dtos.response.chat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MessageDTO {

    private Long id;

    private RoomDto room;

    // USER / SYSTEM (FE dùng để hiển thị bên trái/bên phải)
    private String senderType;

    // USER_xxx / GUEST_xxx / SYSTEM
    private String senderKey;

    // Nội dung tin nhắn
    private String content;

    // text, image, file, system-notification...
    private String type;

    // Thời gian gửi tin
    private LocalDateTime createdAt;
}

