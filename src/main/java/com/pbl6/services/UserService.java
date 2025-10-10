package com.pbl6.services;

import com.pbl6.dtos.request.auth.LoginRequest;
import com.pbl6.dtos.request.auth.RegisterRequest;
import com.pbl6.dtos.response.LoginDto;
import com.pbl6.entities.UserEntity;

public interface UserService {
    void createUser(RegisterRequest registerRequest);
    LoginDto login(LoginRequest loginRequest);
    UserEntity loadUserByPhone(String phone);
    UserEntity createOrGetGuest(String email, String phone, String name);
}
