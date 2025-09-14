package com.pbl6.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginDto {
    private String accessToken = "";
    private String refreshToken = "";
}
