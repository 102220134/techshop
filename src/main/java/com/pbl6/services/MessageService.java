package com.pbl6.services;

import com.pbl6.dtos.response.chat.ChatbotResponseDTO;
import com.pbl6.dtos.response.chat.MessageDTO;
import com.pbl6.entities.MessageEntity;

import java.util.List;

public interface MessageService {
    MessageDTO saveUserMessage(Long roomId, String userKey, String content);
    MessageDTO saveSystemMessage(Long roomId, String staffKey, String content);
    MessageDTO saveBotMessage(Long roomId, ChatbotResponseDTO botResponse);
    List<MessageDTO> getHistory(Long roomId);
}
