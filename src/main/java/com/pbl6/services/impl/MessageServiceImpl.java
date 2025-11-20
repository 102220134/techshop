package com.pbl6.services.impl;

import com.pbl6.dtos.response.chat.MessageDTO;
import com.pbl6.dtos.response.chat.RoomDto;
import com.pbl6.entities.MessageEntity;
import com.pbl6.entities.RoomEntity;
import com.pbl6.repositories.MessageRepository;
import com.pbl6.repositories.RoomRepository;
import com.pbl6.services.MessageService;
import com.pbl6.services.RoomService;
import com.pbl6.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserService userService;

    @Override
    public MessageDTO saveUserMessage(Long roomId, String userKey, String content) {

        // 1) Lưu message user
        MessageEntity msg = new MessageEntity();
        msg.setRoomId(roomId);
        msg.setSenderType("USER");
        msg.setSenderKey(userKey);
        msg.setContent(content);
        msg.setType("text");
        msg.setCreatedAt(LocalDateTime.now());

        MessageEntity saved = messageRepository.save(msg);

        // 2) Update room
        RoomEntity room = roomRepository.findById(roomId).orElseThrow();
        room.setLastMessage(content);
        room.setLastMessageTime(LocalDateTime.now());
        room.setUnreadCount(room.getUnreadCount() + 1);
        roomRepository.save(room);

        return toDTO(saved);
    }

    @Override
    public MessageDTO saveSystemMessage(Long roomId, String staffKey, String content) {

        // 1) Lưu message SYSTEM
        MessageEntity msg = new MessageEntity();
        msg.setRoomId(roomId);
        msg.setSenderType("SYSTEM");
        msg.setSenderKey("SYSTEM");
        msg.setContent(content);
        msg.setType("text");
        msg.setCreatedAt(LocalDateTime.now());

        MessageEntity saved = messageRepository.save(msg);

        // 2) Update room (KHÔNG TĂNG UNREAD)
        RoomEntity room = roomRepository.findById(roomId).orElseThrow();
        room.setLastMessage(content);
        room.setLastMessageTime(LocalDateTime.now());
        room.setUnreadCount(0);  // staff trả lời → reset unread
        roomRepository.save(room);

        return toDTO(saved);
    }

    @Override
    public List<MessageDTO> getHistory(Long roomId) {
        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private MessageDTO toDTO(MessageEntity msg) {
        RoomEntity room = roomRepository.findById(msg.getRoomId()).get();
        String name = null;
        if(room.getUserKey().startsWith("GUEST_")) {
            name = "Khách hàng";
        }
        if(room.getUserKey().startsWith("USER_")) {
            String phone = room.getUserKey().substring(5);
            name = userService.loadUserByPhone(phone).getName();
        }
        RoomDto roomDto =  new RoomDto(
                room.getId(),
                name,
                room.getUserKey(),
                room.getLastMessage(),
                room.getLastMessageTime(),
                room.getUnreadCount()
        );
        return new MessageDTO(
                msg.getId(),
                roomDto,
                msg.getSenderType(),
                msg.getSenderKey(),
                msg.getContent(),
                msg.getType(),
                msg.getCreatedAt()
        );
    }
}

