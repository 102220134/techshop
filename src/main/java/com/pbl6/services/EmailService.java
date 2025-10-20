package com.pbl6.services;

public interface EmailService {
    public void resetPassword(String to, String subject, String text);
}
