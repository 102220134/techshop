package com.pbl6.controllers.chat;

import com.pbl6.dtos.response.chat.MessageDTO;
import com.pbl6.entities.MessageEntity;
import com.pbl6.entities.RoomEntity;
import com.pbl6.services.MessageService;
import com.pbl6.services.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final RoomService roomService;
    private final MessageService messageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat.load_history")
    public void loadHistory(Principal principal) {

        String userKey = principal.getName();

        RoomEntity room = roomService.findOrCreateRoom(userKey);
        List<MessageDTO> history = messageService.getHistory(room.getId());

        simpMessagingTemplate.convertAndSendToUser(
                userKey,
                "/queue/chat_init",
                history
        );
    }

    @MessageMapping("/chat.load_history_by_roomId")
    public void loadHistoryByRoomId(@Payload Map<String, Object> payload, Principal principal) {

        String staffKey = principal.getName();
        Long roomId = Long.valueOf(payload.get("roomId").toString());
        // lấy lịch sử đúng roomId STAFF chọn
        List<MessageDTO> history = messageService.getHistory(roomId);

        // gửi trả về STAFF
        simpMessagingTemplate.convertAndSendToUser(
                "SYSTEM",
                "/queue/staff_chat_init",   // CHANNEL MỚI dành cho STAFF
                history
        );
    }

    @MessageMapping("/chat.send")
    public void userSendMessage(@Payload Map<String, Object> payload, Principal principal) {

        String userKey = principal.getName();           // USER_x / GUEST_x

        if(userKey.startsWith("USER_")||userKey.startsWith("GUEST_")){
            // 1) LƯU TIN NHẮN
            Long roomId = roomService.findOrCreateRoom(userKey).getId();
            String content = payload.get("content").toString();

            MessageDTO message = messageService.saveUserMessage(roomId, userKey, content);
            // 3) Gửi ngược lại UI USER (nếu cần)
            simpMessagingTemplate.convertAndSendToUser(
                    userKey,
                    "/queue/chat",
                    message
            );

            // 4) ĐẨY REALTIME CHO STAFF
            // Staff nhận theo queue — không ai xem trộm được
            simpMessagingTemplate.convertAndSendToUser(
                    "SYSTEM",
                    "/queue/chat",
                    message             // chứa cả roomId + content
            );
        }

        if(userKey.equals("SYSTEM")){
            Long roomId = payload.get("roomId") != null ? Long.valueOf(payload.get("roomId").toString()) : null;
            if(roomId != null){
                String content = payload.get("content").toString();
               MessageDTO message = messageService.saveSystemMessage(roomId, null, content);
                // 3) Gửi ngược lại UI USER (nếu cần)
                simpMessagingTemplate.convertAndSendToUser(
                        message.getRoom().getUserKey(),
                        "/queue/chat",
                        message
                );

                // 4) ĐẨY REALTIME CHO STAFF
                // Staff nhận theo queue — không ai xem trộm được
                simpMessagingTemplate.convertAndSendToUser(
                        "SYSTEM",
                        "/queue/chat",
                        message             // chứa cả roomId + content
                );
            }
        }

    }

    @MessageMapping("/chat.mark_read")
    public void markRoomAsRead(@Payload Map<String, Object> payload, Principal principal) {

        String staffKey = principal.getName();
        Long roomId = Long.valueOf(payload.get("roomId").toString());

        // đánh dấu đã đọc trong DB
        roomService.markAsRead(roomId);

        // thông báo lại cho staff (cập nhật UI)
        simpMessagingTemplate.convertAndSendToUser(
                staffKey,
                "/queue/staff_room_read",
                Map.of("roomId", roomId)
        );
    }


}
