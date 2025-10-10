package com.pbl6.services.impl;

import com.pbl6.services.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToTopic(String topic, Object message) {
        String dest = "/topic/" + topic;
        log.info("🔔 Sending WebSocket message to {}: {}", dest, message);
        messagingTemplate.convertAndSend(dest, message);
    }
}

