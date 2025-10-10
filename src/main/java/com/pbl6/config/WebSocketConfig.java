package com.pbl6.config;

import com.pbl6.filters.CustomHandshakeHandler;
import com.pbl6.filters.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final CustomHandshakeHandler customHandshakeHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ Public socket: không cần token
        registry.addEndpoint("/ws/public")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // ✅ Private socket: cần username
        registry.addEndpoint("/ws/private")
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(customHandshakeHandler)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Client gửi message qua prefix này
        registry.setApplicationDestinationPrefixes("/app");

        // Server gửi message xuống client qua các kênh này
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setUserDestinationPrefix("/user");
    }
}
