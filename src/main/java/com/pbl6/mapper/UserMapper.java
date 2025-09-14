package com.pbl6.mapper;

import com.pbl6.dtos.request.auth.RegisterRequest;
import com.pbl6.entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserEntity toUserEntity(RegisterRequest registerRequest){
        UserEntity userEntity = new UserEntity();
        userEntity.setName(registerRequest.getName());
        userEntity.setEmail(registerRequest.getEmail());
        userEntity.setPassword(registerRequest.getPassword());
        userEntity.setPhone(registerRequest.getPhone());
        return userEntity;
    };
}
