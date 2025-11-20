package com.pbl6.filters;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
@Component
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest req,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        String principalName = (String) attributes.get("principal");

        if (principalName == null) {
            return null;
        }
        return ()-> principalName;
    }
}

