package com.pbl6.dtos.response.auth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginDto {
    private String accessToken = "";
    private String refreshToken = "";
    private UserLogin userInfo;
}
