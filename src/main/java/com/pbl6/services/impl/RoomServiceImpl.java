package com.pbl6.services.impl;

import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.chat.RoomDto;
import com.pbl6.entities.MessageEntity;
import com.pbl6.entities.RoomEntity;
import com.pbl6.repositories.RoomRepository;
import com.pbl6.services.MessageService;
import com.pbl6.services.RoomService;
import com.pbl6.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepo;
    private final MessageService messageService;
    private final UserService userService;

    public RoomEntity findOrCreateRoom(String userKey) {
        RoomEntity room = roomRepo.findByUserKey(userKey);
        if (room == null) {
            room = new RoomEntity();
            room.setUserKey(userKey);
            room.setStatus("active");
            room.setLastMessage("Bắt đầu hội thoại");
            room.setLastMessageTime(LocalDateTime.now());
            room.setUnreadCount(0);
            roomRepo.save(room);

            messageService.saveSystemMessage(room.getId(),null,"Chào bạn");
        }
        return room;
    }

    @Override
    public PageDto<RoomDto> getAllRooms(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC , "lastMessageTime"
        );
        PageRequest pageable = PageRequest.of(page - 1, size, sort);
        Page<RoomDto> rs = roomRepo.findAll(pageable).map(this::roomDto);
        return new PageDto<>(rs);
    }

    @Override
    public void markAsRead(Long roomId) {
        RoomEntity room = roomRepo.findById(roomId).get();
        room.setUnreadCount(0);
        roomRepo.save(room);
    }

    public RoomDto roomDto(RoomEntity room) {
        String name = null;
        if(room.getUserKey().startsWith("GUEST_")) {
            name = "Khách hàng";
        }
        if(room.getUserKey().startsWith("USER_")) {
            String phone = room.getUserKey().substring(5);
            name = userService.loadUserByPhone(phone).getName();
        }
        return new RoomDto(
                room.getId(),
                name,
                room.getUserKey(),
                room.getLastMessage(),
                room.getLastMessageTime(),
                room.getUnreadCount()
        );
    }

//    public Long getRoomIdByUserKey(String userKey) {
//        return roomRepo.findByUserKey(userKey).getId();
//    }
//
//    public String getUserKeyByRoomId(Long roomId) {
//        return roomRepo.findById(roomId).orElseThrow().getUserKey();
//    }
//
//    // Khi USER gửi tin nhắn
//    public void updateRoomAfterUserMessage(Long roomId, String content) {
//        Room room = roomRepo.findById(roomId).orElseThrow();
//
//        room.setLastMessage(content);
//        room.setLastMessageTime(LocalDateTime.now());
//        room.setUnreadCount(room.getUnreadCount() + 1);
//
//        roomRepo.save(room);
//    }
//
//    // Khi STAFF trả lời tin ↓
//    public void updateRoomAfterStaffReply(Long roomId, String content) {
//        Room room = roomRepo.findById(roomId).orElseThrow();
//
//        room.setLastMessage(content);
//        room.setLastMessageTime(LocalDateTime.now());
//        room.setUnreadCount(0); // staff đã xem → đọc hết
//
//        roomRepo.save(room);
//    }
//
//    public void claimRoom(Long roomId, String staffKey) {
//        Room room = roomRepo.findById(roomId).orElseThrow();
//        room.setClaimedBy(staffKey);
//        roomRepo.save(room);
//    }
//
//    public void releaseRoom(Long roomId) {
//        Room room = roomRepo.findById(roomId).orElseThrow();
//        room.setClaimedBy(null);
//        roomRepo.save(room);
//    }
}
