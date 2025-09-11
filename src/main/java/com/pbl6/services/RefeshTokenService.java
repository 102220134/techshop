package com.pbl6.services;

import com.pbl6.dtos.response.LoginDto;
import com.pbl6.entities.UserEntity;

public interface RefeshTokenService {
    String addRefreshToken(UserEntity user);
    LoginDto refreshToken(String token);
}
