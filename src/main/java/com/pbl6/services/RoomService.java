package com.pbl6.services;

import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.chat.RoomDto;
import com.pbl6.entities.RoomEntity;

public interface RoomService {
    RoomEntity findOrCreateRoom(String userKey);
    PageDto<RoomDto> getAllRooms(int page, int size);
    void markAsRead(Long roomId);
}
