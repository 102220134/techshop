package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.auth.RegisterRequest;
import com.pbl6.dtos.request.user.*;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.user.UserDetailDto;
import com.pbl6.dtos.response.user.UserDto;
import com.pbl6.services.UserService;
import com.pbl6.utils.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/private/user")
@RequiredArgsConstructor
@Tag(name = "Quản lý người dùng")
public class UserController {
    private final AuthenticationUtil authenticationUtil;
    private final UserService userService;

    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    @GetMapping("customer")
    @Operation(summary = "Xem danh sách khách hàng", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<PageDto<UserDto>> getAllCustomers(@ParameterObject SearchUserRequest request) {
        return new ApiResponseDto<>(userService.searchCustomers(request));
    }

    @PreAuthorize("hasAuthority('STAFF_READ')")
    @GetMapping("/staff")
    @Operation(summary = "Xem danh sách nhan vien", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<PageDto<UserDto>> getAllStaffs(@ParameterObject SearchUserRequest request) {
        return new ApiResponseDto<>(userService.searchStaffs(request));
    }

    @PreAuthorize("hasAnyAuthority('STAFF_READ','CUSTOMER_READ')")
    @GetMapping("/{userId}")
    @Operation(summary = "Xem thông tin người dùng", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDetailDto> getUserInfo(@PathVariable Long userId) {
        return new ApiResponseDto<>(userService.getUserInfo(userId));
    }


    @PreAuthorize("hasAuthority('STAFF_CREATE')")
    @PostMapping("/create-staff")
    @Operation(summary = "Tạo tài khoản nhân viên", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDto> createStaffUser(@Valid  @RequestBody CreateStaffRequest request) {
        return new ApiResponseDto<>(userService.createStaff(request));
    }

    @PreAuthorize("hasAuthority('STAFF_UPDATE')")
    @PutMapping("/update-staff/{id}")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDto> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStaffRequest request
    ) {
        return new ApiResponseDto<>(userService.updateStaff(id, request));
    }


    @PreAuthorize("hasAuthority('CUSTOMER_CREATE')")
    @PostMapping("/create-customer")
    @Operation(summary = "Tạo tài khoản user", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDto> createUser(@RequestBody RegisterRequest request) {
        return new ApiResponseDto<>(userService.createUser(request));
    }

    @PreAuthorize("hasAuthority('CUSTOMER_CREATE')")
    @PostMapping("/create-guest")
    @Operation(summary = "Tạo thông tin khách vãng lai", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDto> createUser(@RequestBody CreateGuestRequest request) {
        return new ApiResponseDto<>(userService.createGuest(request));
    }

    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    @PutMapping("/update-info/{userId}")
    @Operation(summary = "Cập nhập thông tin user", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDetailDto> updateUserInfo(@RequestBody UserUpdateInfoRequest request, @PathVariable Long userId) {
        return new ApiResponseDto<>(userService.updateUserInfo(userId,request));
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER_UPDATE', 'STAFF_UPDATE')")
    @PutMapping("/update-status/{userId}")
    @Operation(summary = "Cập nhật trạng thái tài khoản user", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDetailDto> updateUserStatus(@RequestBody UserUpdateStatusRequest request, @PathVariable Long userId) {
        return new ApiResponseDto<>(userService.updateUserStatus(userId,request));
    }

}
