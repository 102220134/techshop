package com.pbl6.controllers.chat;
import com.pbl6.dtos.response.chat.MessageDTO;
import com.pbl6.entities.RoomEntity;
import com.pbl6.services.MessageService;
import com.pbl6.services.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.security.Principal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

}

