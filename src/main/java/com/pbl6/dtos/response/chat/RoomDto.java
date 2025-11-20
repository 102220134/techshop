package com.pbl6.dtos.response.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RoomDto {
    private long id;
    private String displayName;
    private String userKey;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
}
