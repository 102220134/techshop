package com.pbl6.controllers.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate template;

    @MessageMapping("/chat")
    public void handleChat(String message) {
        log.info("socket:"+message);
        template.convertAndSend("/topic/public", "📢 " + message);
    }
}

