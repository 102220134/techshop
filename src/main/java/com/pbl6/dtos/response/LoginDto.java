package com.pbl6.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginDto {
//    private boolean isAuthenticated = false;
//    private String role;
    private String accessToken = "";
    private String refreshToken = "";
}
