package com.pbl6.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbl6.dtos.response.chat.ChatbotResponseDTO;
import com.pbl6.dtos.response.chat.ChatbotApiResponse;
import com.pbl6.entities.RoomEntity;
import com.pbl6.repositories.RoomRepository;
import com.pbl6.services.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {

    @Value("${chatbot.api.url}")
    private String chatbotApiUrl;

    @Value("${chatbot.api.key}")
    private String chatbotApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RoomRepository roomRepository;

    @Override
    public ChatbotResponseDTO getChatbotResponse(String userMessage, String userKey, String conversationId) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", userMessage);
            requestBody.put("inputs", new HashMap<>());
            requestBody.put("user", userKey);
            requestBody.put("response_mode", "blocking");
            
            if (conversationId != null && !conversationId.isEmpty()) {
                requestBody.put("conversation_id", conversationId);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + chatbotApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String fullUrl = chatbotApiUrl + "/chat-messages";
            log.info("Calling chatbot API for user: {}", userKey);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ChatbotApiResponse apiResponse = objectMapper.readValue(response.getBody(), ChatbotApiResponse.class);
                String answer = apiResponse.getAnswer();
                String newConversationId = apiResponse.getConversationId();

                saveConversationId(userKey, newConversationId);
                ChatbotResponseDTO chatbotResponse = parseAnswerToResponse(answer);
                
                return chatbotResponse;
            } else {
                log.error("Chatbot API returned non-OK status: {}", response.getStatusCode());
                return createErrorResponse("Xin lỗi, hệ thống đang bận. Vui lòng thử lại sau.");
            }

        } catch (Exception e) {
            log.error("Error calling chatbot API", e);
            return createErrorResponse("Xin lỗi, có lỗi xảy ra khi xử lý yêu cầu của bạn.");
        }
    }

    private void saveConversationId(String userKey, String conversationId) {
        try {
            RoomEntity room = roomRepository.findByUserKey(userKey);
            if (room != null && conversationId != null) {
                room.setConversationId(conversationId);
                roomRepository.save(room);
            }
        } catch (Exception e) {
            log.error("Failed to save conversation ID", e);
        }
    }

    private ChatbotResponseDTO parseAnswerToResponse(String answer) {
        try {
            return objectMapper.readValue(answer, ChatbotResponseDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse chatbot response as JSON: {}", answer, e);
            
            ChatbotResponseDTO fallback = new ChatbotResponseDTO();
            fallback.setReplyText(answer);
            fallback.setSuggestedProducts(null);
            return fallback;
        }
    }

    private ChatbotResponseDTO createErrorResponse(String message) {
        ChatbotResponseDTO error = new ChatbotResponseDTO();
        error.setReplyText(message);
        error.setSuggestedProducts(null);
        return error;
    }
}
