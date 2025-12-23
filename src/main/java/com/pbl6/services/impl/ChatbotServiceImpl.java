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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
        // Nếu là conversation mới (conversationId = null), thử với retry
        if (conversationId == null || conversationId.isEmpty()) {
            return getChatbotResponseWithRetry(userMessage, userKey, conversationId, 2);
        }
        
        // Nếu đã có conversationId, gọi trực tiếp
        return callDifyApi(userMessage, userKey, conversationId);
    }
    
    /**
     * Gọi Dify API với retry logic cho conversation mới
     */
    private ChatbotResponseDTO getChatbotResponseWithRetry(String userMessage, String userKey, String conversationId, int maxRetries) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxRetries) {
            attempt++;
            try {
                log.info("Attempt {} to create new conversation for user: {}", attempt, userKey);
                ChatbotResponseDTO response = callDifyApi(userMessage, userKey, conversationId);
                
                // Nếu thành công, return ngay
                if (response != null && response.getReplyText() != null && !response.getReplyText().startsWith("Xin lỗi")) {
                    return response;
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} failed for user: {}. Error: {}", attempt, userKey, e.getMessage());
                
                // Đợi một chút trước khi retry
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000); // 1 second
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        // Nếu tất cả attempts đều fail, log và return error
        log.error("All {} attempts failed for new conversation with user: {}", maxRetries, userKey, lastException);
        return createErrorResponse("Xin lỗi, không thể kết nối với chatbot lúc này. Vui lòng thử lại sau hoặc chuyển sang chế độ nhân viên.");
    }
    
    /**
     * Gọi Dify API
     */
    private ChatbotResponseDTO callDifyApi(String userMessage, String userKey, String conversationId) {
        try {
            // Luôn sử dụng test_user_123 cho mọi request
            String userForApi = "test_user_123";
            log.info("Using fixed user key '{}' for API call (original userKey: {})", userForApi, userKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", userMessage);
            requestBody.put("inputs", new HashMap<>());
            requestBody.put("user", userForApi);
            requestBody.put("response_mode", "blocking");
            
            if (conversationId != null && !conversationId.isEmpty()) {
                requestBody.put("conversation_id", conversationId);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + chatbotApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String fullUrl = chatbotApiUrl + "/chat-messages";
            log.info("Calling chatbot API for user: {} (using fixed: {}) with conversationId: {}", 
                userKey, userForApi, conversationId != null ? conversationId : "(new conversation)");
            log.debug("Request body: {}", requestBody);
            
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
                log.error("Chatbot API returned non-OK status: {} for user: {}", response.getStatusCode(), userKey);
                return createErrorResponse("Xin lỗi, hệ thống đang bận. Vui lòng thử lại sau.");
            }

        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("Dify API server error (500) for user: {}. Response: {}", userKey, e.getResponseBodyAsString());
            throw new RuntimeException("Dify API 500 error", e);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Dify API client error ({}) for user: {}. Response: {}", e.getStatusCode(), userKey, e.getResponseBodyAsString());
            throw new RuntimeException("Dify API client error", e);
        } catch (Exception e) {
            log.error("Unexpected error calling chatbot API for user: {}", userKey, e);
            throw new RuntimeException("Unexpected error", e);
        }
    }

    private void saveConversationId(String userKey, String conversationId) {
        try {
            RoomEntity room = roomRepository.findByUserKey(userKey);
            if (room != null && conversationId != null && !conversationId.isEmpty()) {
                log.info("Saving conversation ID: {} for user: {}", conversationId, userKey);
                room.setConversationId(conversationId);
                roomRepository.save(room);
                log.info("Successfully saved conversation ID for user: {}", userKey);
            } else {
                log.warn("Cannot save conversation ID - room: {}, conversationId: {}", room != null, conversationId);
            }
        } catch (Exception e) {
            log.error("Failed to save conversation ID for user: {}", userKey, e);
        }
    }

    private ChatbotResponseDTO parseAnswerToResponse(String answer) {
        try {
            // Extract JSON from markdown code block if present
            String jsonContent = extractJsonFromMarkdown(answer);
            return objectMapper.readValue(jsonContent, ChatbotResponseDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse chatbot response as JSON: {}", answer, e);
            
            // Fallback: return plain text as reply
            ChatbotResponseDTO fallback = new ChatbotResponseDTO();
            fallback.setReplyText(answer);
            fallback.setSuggestedProducts(null);
            return fallback;
        }
    }

    private String extractJsonFromMarkdown(String text) {
        // Check if text contains markdown JSON block with ```json tag
        if (text.contains("```json")) {
            int startIndex = text.indexOf("```json");
            int endIndex = text.indexOf("```", startIndex + 7);
            
            if (startIndex != -1 && endIndex != -1) {
                // Extract content between ```json and ```
                String jsonBlock = text.substring(startIndex + 7, endIndex).trim();
                log.info("Extracted JSON from markdown block with json tag");
                return jsonBlock;
            }
        }
        
        // Check if text contains plain JSON block (without json tag)
        if (text.contains("```")) {
            int startIndex = text.indexOf("```");
            int endIndex = text.indexOf("```", startIndex + 3);
            
            if (startIndex != -1 && endIndex != -1) {
                String possibleJson = text.substring(startIndex + 3, endIndex).trim();
                // Check if it looks like JSON (starts with { or [)
                if (possibleJson.startsWith("{") || possibleJson.startsWith("[")) {
                    log.info("Extracted JSON from plain markdown block");
                    return possibleJson;
                }
            }
        }
        
        // Try to find JSON object directly in text (starting with {)
        int jsonStart = text.indexOf("{");
        if (jsonStart != -1) {
            // Find matching closing brace
            int braceCount = 0;
            int jsonEnd = -1;
            for (int i = jsonStart; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '{') braceCount++;
                else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        jsonEnd = i + 1;
                        break;
                    }
                }
            }
            
            if (jsonEnd != -1) {
                String jsonBlock = text.substring(jsonStart, jsonEnd).trim();
                log.info("Extracted JSON object directly from text");
                return jsonBlock;
            }
        }
        
        // No JSON found, return original text (will be used as plain reply)
        log.info("No JSON found in response, treating as plain text");
        return text;
    }

    private ChatbotResponseDTO createErrorResponse(String message) {
        ChatbotResponseDTO error = new ChatbotResponseDTO();
        error.setReplyText(message);
        error.setSuggestedProducts(null);
        return error;
    }

    /**
     * Sanitize user key để tránh lỗi với Dify API
     * Dify có thể có giới hạn về độ dài (thường là 64 ký tự) hoặc ký tự đặc biệt
     * Nếu user key quá dài, sẽ hash MD5 để rút ngắn
     */
    private String sanitizeUserKey(String userKey) {
        if (userKey == null || userKey.isEmpty()) {
            return "anonymous";
        }
        
        // Xóa các ký tự đặc biệt, chỉ giữ alphanumeric, underscore và dash
        String sanitized = userKey.replaceAll("[^a-zA-Z0-9_-]", "_");
        
        // Nếu quá dài (> 32 ký tự), hash MD5 phần sau
        if (sanitized.length() > 32) {
            try {
                String prefix = sanitized.substring(0, 16); // Giữ prefix để dễ debug
                String toHash = sanitized.substring(16);
                
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] hashBytes = md.digest(toHash.getBytes(StandardCharsets.UTF_8));
                
                // Convert to hex string (first 8 chars only)
                StringBuilder hexString = new StringBuilder();
                for (int i = 0; i < Math.min(4, hashBytes.length); i++) {
                    String hex = Integer.toHexString(0xff & hashBytes[i]);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                
                String result = prefix + "_" + hexString.toString();
                log.debug("Sanitized user key: {} -> {}", userKey, result);
                return result;
            } catch (Exception e) {
                log.warn("Failed to hash user key, using truncated version", e);
                return sanitized.substring(0, 32);
            }
        }
        
        return sanitized;
    }
    
    /**
     * Rút ngắn user key xuống tối đa 16 ký tự để tránh lỗi từ Dify API
     * Format: GUEST_xxxxx (giữ prefix GUEST_ và lấy phần cuối)
     */
    private String getShortUserKey(String userKey) {
        if (userKey == null || userKey.length() <= 16) {
            return userKey;
        }
        
        // Giữ prefix (GUEST_ hoặc USER_) và lấy 10 ký tự cuối
        if (userKey.startsWith("GUEST_")) {
            String suffix = userKey.substring(6); // Bỏ "GUEST_"
            if (suffix.length() <= 10) {
                return userKey;
            }
            // Lấy 10 ký tự cuối của suffix
            return "GUEST_" + suffix.substring(suffix.length() - 10);
        } else if (userKey.startsWith("USER_")) {
            String suffix = userKey.substring(5); // Bỏ "USER_"
            if (suffix.length() <= 11) {
                return userKey;
            }
            // Lấy 11 ký tự cuối của suffix
            return "USER_" + suffix.substring(suffix.length() - 11);
        }
        
        // Fallback: lấy 16 ký tự cuối
        return userKey.substring(userKey.length() - 16);
    }
}
