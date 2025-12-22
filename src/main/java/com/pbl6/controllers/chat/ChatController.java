package com.pbl6.controllers.chat;

import com.pbl6.dtos.response.chat.ChatbotResponseDTO;
import com.pbl6.dtos.response.chat.MessageDTO;
import com.pbl6.entities.MessageEntity;
import com.pbl6.entities.RoomEntity;
import com.pbl6.services.ChatbotService;
import com.pbl6.services.MessageService;
import com.pbl6.services.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ChatController {
    private final RoomService roomService;
    private final MessageService messageService;
    private final ChatbotService chatbotService;
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
        List<MessageDTO> history = messageService.getHistory(roomId);

        simpMessagingTemplate.convertAndSendToUser(
                "SYSTEM",
                "/queue/staff_chat_init",
                history
        );
    }

    @MessageMapping("/chat.send")
    public void userSendMessage(@Payload Map<String, Object> payload, Principal principal) {
        String userKey = principal.getName();

        if(userKey.startsWith("USER_")||userKey.startsWith("GUEST_")){
            RoomEntity room = roomService.findOrCreateRoom(userKey);
            String content = payload.get("content").toString();

            MessageDTO userMessage = messageService.saveUserMessage(room.getId(), userKey, content);
            
            simpMessagingTemplate.convertAndSendToUser(
                    userKey,
                    "/queue/chat",
                    userMessage
            );

            if ("bot".equalsIgnoreCase(room.getChatMode())) {
                try {
                    ChatbotResponseDTO botResponse = chatbotService.getChatbotResponse(
                            content, 
                            userKey, 
                            room.getConversationId()
                    );
                    
                    MessageDTO botMessage = messageService.saveBotMessage(room.getId(), botResponse);
                    
                    simpMessagingTemplate.convertAndSendToUser(
                            userKey,
                            "/queue/chat",
                            botMessage
                    );
                    
                    log.info("Bot responded to user: {}", userKey);
                    
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    log.error("Chatbot error for user: " + userKey, e);
                    
                    ChatbotResponseDTO errorResponse = new ChatbotResponseDTO();
                    errorResponse.setReplyText("Xin lỗi, bot đang bận. Vui lòng chuyển sang chế độ nhân viên hoặc thử lại sau.");
                    
                    MessageDTO errorMessage = messageService.saveBotMessage(room.getId(), errorResponse);
                    simpMessagingTemplate.convertAndSendToUser(
                            userKey,
                            "/queue/chat",
                            errorMessage
                    );
                }
                
            } else {
                simpMessagingTemplate.convertAndSendToUser(
                        "SYSTEM",
                        "/queue/chat",
                        userMessage
                );
            }
        }

        if(userKey.equals("SYSTEM")){
            Long roomId = payload.get("roomId") != null ? Long.valueOf(payload.get("roomId").toString()) : null;
            if(roomId != null){
                String content = payload.get("content").toString();
                MessageDTO message = messageService.saveSystemMessage(roomId, null, content);
                
                simpMessagingTemplate.convertAndSendToUser(
                        message.getRoom().getUserKey(),
                        "/queue/chat",
                        message
                );

                simpMessagingTemplate.convertAndSendToUser(
                        "SYSTEM",
                        "/queue/chat",
                        message
                );
            }
        }

    }

    @MessageMapping("/chat.mark_read")
    public void markRoomAsRead(@Payload Map<String, Object> payload, Principal principal) {
        String staffKey = principal.getName();
        Long roomId = Long.valueOf(payload.get("roomId").toString());

        roomService.markAsRead(roomId);

        simpMessagingTemplate.convertAndSendToUser(
                staffKey,
                "/queue/staff_room_read",
                Map.of("roomId", roomId)
        );
    }

    @MessageMapping("/chat.switch_mode")
    public void switchChatMode(@Payload Map<String, Object> payload, Principal principal) {
        String userKey = principal.getName();
        String chatMode = payload.get("chatMode").toString();
        
        try {
            RoomEntity room = roomService.switchChatMode(userKey, chatMode);
            
            // Gửi xác nhận về cho user
            simpMessagingTemplate.convertAndSendToUser(
                    userKey,
                    "/queue/chat_mode_changed",
                    Map.of(
                            "chatMode", room.getChatMode(),
                            "message", "bot".equals(room.getChatMode()) 
                                    ? "Đã chuyển sang chế độ Bot tự động" 
                                    : "Đã chuyển sang chế độ tư vấn viên"
                    )
            );
            
            log.info("User {} switched to {} mode", userKey, chatMode);
            
        } catch (Exception e) {
            log.error("Failed to switch chat mode for user: " + userKey, e);
            simpMessagingTemplate.convertAndSendToUser(
                    userKey,
                    "/queue/chat_error",
                    Map.of("error", "Không thể chuyển đổi chế độ chat")
            );
        }
    }


}
