package com.pbl6.services;

import com.pbl6.dtos.request.auth.LoginRequest;
import com.pbl6.dtos.response.LoginDto;
import com.pbl6.entities.UserEntity;

public interface AuthService {
    String addRefreshToken(UserEntity user);
    LoginDto refreshToken(String token);
    public void resetPasswordAndSend(String email);
    LoginDto login(LoginRequest loginRequest);
}
