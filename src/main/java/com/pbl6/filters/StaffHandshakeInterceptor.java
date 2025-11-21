package com.pbl6.filters;

import com.pbl6.entities.UserEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.services.UserService;
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
public class StaffHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        String tokenParam = servletRequest.getServletRequest().getParameter("token");

        if (tokenParam == null || tokenParam.isBlank()) {
            reject(response, 401, "Missing token");
            return false;
        }

        try {
            String phone = jwtUtil.extractPhone(tokenParam);
            UserEntity user = userService.loadUserByPhone(phone);

            if (!user.hasAuthority("CHAT")) {
                reject(response, 403, "No permission");
                return false;
            }

            attributes.put("principal", "SYSTEM");
            return true;

        } catch (Exception ex) {
            reject(response, 401, "Invalid or expired token");
            return false;
        }
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
                               WebSocketHandler wsHandler, Exception exception) {
    }
}


