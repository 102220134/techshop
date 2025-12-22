package com.pbl6.dtos.response.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatbotApiResponse {
    private String answer;
    
    @JsonProperty("conversation_id")
    private String conversationId;
    
    @JsonProperty("message_id")
    private String messageId;
    
    private String mode;
    
    @JsonProperty("created_at")
    private Long createdAt;
}
