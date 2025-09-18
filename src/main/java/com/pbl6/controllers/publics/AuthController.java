package com.pbl6.controllers.publics;

import com.pbl6.dtos.request.auth.LoginRequest;
import com.pbl6.dtos.request.auth.RefreshTokenRequest;
import com.pbl6.dtos.request.auth.RegisterRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.LoginDto;
import com.pbl6.services.RefreshTokenService;
import com.pbl6.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/public/auth")
public class AuthController {
    private final UserService userService;
    private final RefreshTokenService refeshTokenService;
    @PostMapping("register")
    public ApiResponseDto<?> Register(@Valid @RequestBody RegisterRequest registerRequest) {
        userService.createUser(registerRequest);
        return new ApiResponseDto<>();
    }
    @PostMapping("login")
    public ApiResponseDto<LoginDto> Login (@Valid @RequestBody LoginRequest loginRequest) {
        LoginDto loginResponse = userService.login(loginRequest);
        ApiResponseDto<LoginDto> response = new ApiResponseDto<>();
        response.setData(loginResponse);
        return response;
    }
    @PostMapping("refresh_token")
    public ApiResponseDto<LoginDto> RefreshToken (@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        LoginDto loginResponse = refeshTokenService.refreshToken(refreshTokenRequest.getRefreshToken());
        ApiResponseDto<LoginDto> response = new ApiResponseDto<>();
        response.setData(loginResponse);
        return response;
    }
}
