package com.pbl6.services;

public interface WebSocketService {
    void sendToTopic(String topic, Object message);
}
