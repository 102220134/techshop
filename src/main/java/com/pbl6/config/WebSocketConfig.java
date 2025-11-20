package com.pbl6.config;

import com.pbl6.filters.CustomHandshakeHandler;
import com.pbl6.filters.CustomerHandshakeInterceptor;
import com.pbl6.filters.StaffHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final CustomerHandshakeInterceptor customerHandshakeInterceptor;
    private final CustomHandshakeHandler customHandshakeHandler;
    private final StaffHandshakeInterceptor staffHandshakeInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ Public socket: không cần token
        registry.addEndpoint("/ws/public")
                .setAllowedOriginPatterns("*")
                .withSockJS().setSessionCookieNeeded(false);

        // ✅ Private socket: cần username
        registry.addEndpoint("/ws/customer")
                .addInterceptors(customerHandshakeInterceptor)
                .setHandshakeHandler(customHandshakeHandler)
                .setAllowedOriginPatterns("*")
                .withSockJS().setSessionCookieNeeded(false);

        registry.addEndpoint("/ws/staff")
                .addInterceptors(staffHandshakeInterceptor)
                .setHandshakeHandler(customHandshakeHandler)
                .setAllowedOriginPatterns("*")
                .withSockJS().setSessionCookieNeeded(false);
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
