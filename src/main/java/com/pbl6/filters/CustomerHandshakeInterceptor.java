package com.pbl6.filters;

import com.pbl6.entities.UserEntity;
import com.pbl6.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;

        String tokenParam = servletRequest.getServletRequest().getParameter("token");
        if (tokenParam != null && !tokenParam.isBlank()) {
            try {
                String phone = jwtUtil.extractPhone(tokenParam);
                attributes.put("principal", "USER_" + phone);
                return true;

            } catch (Exception ex) {
                reject(response, 401, "Invalid or expired token");
                return false;
            }
        }

        String guestId = servletRequest.getServletRequest().getParameter("guestId");
        if (guestId != null && !guestId.isBlank()) {
            attributes.put("principal", "GUEST_" + guestId);
            return true;
        }

        return false;

    }
    private void reject(ServerHttpResponse response, int status, String message) {
        try {
            response.setStatusCode(HttpStatus.valueOf(status));
            response.getHeaders().add("Sec-WebSocket-Accept", ""); // bắt buộc để SockJS hiểu handshake reject
            response.getBody().write(message.getBytes());
        } catch (Exception ignored) {}
    }
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}


