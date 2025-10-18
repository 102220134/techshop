package com.pbl6.controllers.customers;

import com.pbl6.dtos.request.profile.ChangePasswordRequest;
import com.pbl6.dtos.request.profile.ProfileUpdateRequest;
import com.pbl6.dtos.request.profile.UserAddressCreateRequest;
import com.pbl6.dtos.request.profile.UserAddressUpdateRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.user.UserAddressDto;
import com.pbl6.dtos.user.UserDto;
import com.pbl6.services.UserService;
import com.pbl6.utils.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/customer/")
@RequiredArgsConstructor
@Tag(name = "Thông tin người dùng")
public class SelfController {
    private final AuthenticationUtil authenticationUtil;
    private final UserService userService;

    @GetMapping("profile")
    @Operation(summary = "Lấy thong tin người dùng", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDto> getProfile() {
        Long userId = authenticationUtil.getCurrentUserId();
        return new ApiResponseDto<>(userService.getUserInfo(userId));
    }

    @PutMapping("profile")
    @Operation(summary = "Cập nhật thông tin người dùng", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDto> updateProfile(@RequestBody ProfileUpdateRequest req) {
        Long userId = authenticationUtil.getCurrentUserId();
        return new ApiResponseDto<>(userService.updateProfile(userId, req));
    }

    @PutMapping("change-password")
    @Operation(summary = "Đổi mật khẩu", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<?> changePassword(@RequestBody ChangePasswordRequest req) {
        Long userId = authenticationUtil.getCurrentUserId();
        userService.changePassword(userId, req);
        return new ApiResponseDto<>("Đổi mật khẩu thành công");
    }

    @PostMapping("myAddress")
    @Operation(summary = "Thêm địa chỉ người dùng", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserAddressDto> createUserAddress(@RequestBody UserAddressCreateRequest req) {
        Long userId = authenticationUtil.getCurrentUserId();
        return new ApiResponseDto<>(userService.createAddress(userId, req));
    }

    @PutMapping("myAddress")
    @Operation(summary = "Cập nhật địa chỉ người dùng", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserAddressDto> updateUserAddress(@RequestBody UserAddressUpdateRequest req) {
        Long userId = authenticationUtil.getCurrentUserId();
        return new ApiResponseDto<>(userService.updateAddress(userId, req));
    }
    @DeleteMapping("myAddress/{id}")
    @Operation(summary = "Xóa địa chỉ người dùng", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<?> deleteUserAddress(@PathVariable Long id) {
        Long userId = authenticationUtil.getCurrentUserId();
        userService.deleteAddress(userId, id);
        return new ApiResponseDto<>("Xóa địa chỉ thành công");
    }

}
