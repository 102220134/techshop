package com.pbl6.services;

import com.pbl6.dtos.response.chat.ChatbotResponseDTO;

public interface ChatbotService {
    /**
     * Gửi tin nhắn đến chatbot và nhận phản hồi
     * @param userMessage Tin nhắn từ user
     * @param userKey USER_xxx hoặc GUEST_xxx
     * @param conversationId ID hội thoại để duy trì ngữ cảnh (nullable)
     * @return ChatbotResponseDTO chứa reply_text và suggested_products
     */
    ChatbotResponseDTO getChatbotResponse(String userMessage, String userKey, String conversationId);
}
