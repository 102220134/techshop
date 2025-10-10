package com.pbl6.controllers.publics;

import com.pbl6.dtos.request.auth.LoginRequest;
import com.pbl6.dtos.request.auth.RefreshTokenRequest;
import com.pbl6.dtos.request.auth.RegisterRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.LoginDto;
import com.pbl6.services.RefreshTokenService;
import com.pbl6.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/public/auth")
@Tag(name = "Authentication", description = "API xác thực: đăng ký, đăng nhập, refresh token")
public class AuthController {
    private final UserService userService;
    private final RefreshTokenService refeshTokenService;

    @Operation(
            summary = "Đăng ký",
            description = "Tạo mới một tài khoản người dùng",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Đăng ký thành công",
                            content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
            }
    )
    @PostMapping("register")
    public ApiResponseDto<?> Register(@Valid @RequestBody RegisterRequest registerRequest) {
        userService.createUser(registerRequest);
        return new ApiResponseDto<>();
    }

    @Operation(
            summary = "Đăng nhập",
            description = "Người dùng đăng nhập bằng username và password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
                            content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
            }
    )
    @PostMapping("login")
    public ApiResponseDto<LoginDto> Login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginDto loginResponse = userService.login(loginRequest);
        ApiResponseDto<LoginDto> response = new ApiResponseDto<>();
        response.setData(loginResponse);
        return response;
    }

    @Operation(
            summary = "Refresh Token",
            description = "Dùng refresh token để lấy access token mới",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Refresh thành công",
                            content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
            }
    )
    @PostMapping("refresh_token")
    public ApiResponseDto<LoginDto> RefreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        LoginDto loginResponse = refeshTokenService.refreshToken(refreshTokenRequest.getRefreshToken());
        ApiResponseDto<LoginDto> response = new ApiResponseDto<>();
        response.setData(loginResponse);
        return response;
    }
}
