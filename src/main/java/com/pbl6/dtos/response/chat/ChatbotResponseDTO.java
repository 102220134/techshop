package com.pbl6.dtos.response.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponseDTO {
    
    @JsonProperty("reply_text")
    private String replyText;
    
    @JsonProperty("suggested_products")
    private List<SuggestedProduct> suggestedProducts;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedProduct {
        private String id;
        private String name;
        private Long price;
        
        @JsonProperty("image_url")
        private String imageUrl;
        
        private String link;
        
        @JsonProperty("short_desc")
        private String shortDesc;
        
        private List<String> highlights;
    }
}
