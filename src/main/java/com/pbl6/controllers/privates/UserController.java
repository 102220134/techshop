package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.auth.RegisterRequest;
import com.pbl6.dtos.request.order.MyOrderRequest;
import com.pbl6.dtos.request.user.SearchUserRequest;
import com.pbl6.dtos.request.user.UserUpdateInfoRequest;
import com.pbl6.dtos.request.user.UserUpdateRoleRequest;
import com.pbl6.dtos.request.user.UserUpdateStatusRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.user.UserDetailDto;
import com.pbl6.dtos.user.UserDto;
import com.pbl6.services.UserService;
import com.pbl6.utils.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
@Tag(name = "Quản lý người dùng")
public class UserController {
    private final AuthenticationUtil authenticationUtil;
    private final UserService userService;

    @PreAuthorize("hasAnyAuthority('USER_READ_ALL','USER_READ_CUSTOMER')")
    @GetMapping("customer")
    @Operation(summary = "Xem danh sách khách hàng", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<PageDto<UserDto>> getAllCustomers(@ParameterObject SearchUserRequest request) {
        return new ApiResponseDto<>(userService.searchCustomers(request));
    }

    @PreAuthorize("hasAuthority('USER_READ_ALL')")
    @GetMapping("/staff")
    @Operation(summary = "Xem danh sách nhan vien", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<PageDto<UserDto>> getAllStaffs(@ParameterObject SearchUserRequest request) {
        return new ApiResponseDto<>(userService.searchStaffs(request));
    }

    @PreAuthorize("hasAnyAuthority('USER_READ_ALL','USER_READ_CUSTOMER')")
    @GetMapping("/{userId}")
    @Operation(summary = "Xem thông tin chi tiết", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDetailDto> getCustomerInfo(@PathVariable Long userId) {
        return new ApiResponseDto<>(userService.getUserInfo(userId));
    }

    @PostMapping("/create")
    @Operation(summary = "Tạo tài khoản user", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDto> createUser(@RequestBody RegisterRequest request) {
        return new ApiResponseDto<>(userService.createUser(request));
    }

    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @PutMapping("/update-info/{userId}")
    @Operation(summary = "Cập nhập thông tin user", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDetailDto> updateUserInfo(@RequestBody UserUpdateInfoRequest request, @PathVariable Long userId) {
        return new ApiResponseDto<>(userService.updateUserInfo(userId,request));
    }

    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @PutMapping("/update-role/{userId}")
    @Operation(summary = "Cập nhập role", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDetailDto> updateUserRole(@RequestBody UserUpdateRoleRequest request, @PathVariable Long userId) {
        return new ApiResponseDto<>(userService.updateUserRole(userId,request));
    }

    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @PutMapping("/update-status/{userId}")
    @Operation(summary = "Cập nhật trạng thái tài khoản user", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDetailDto> updateUserStatus(@RequestBody UserUpdateStatusRequest request, @PathVariable Long userId) {
        return new ApiResponseDto<>(userService.updateUserStatus(userId,request));
    }

//    @PreAuthorize("hasAuthority('USER_DELETE')")
//    @DeleteMapping("/delete/{userId}")
//    @Operation(summary = "Xóa tài khoản user", security = { @SecurityRequirement(name = "bearerAuth") })
//    public ApiResponseDto<?> deleteUser(@PathVariable Long userId) {
//        userService.deleteUser(userId);
//        return new ApiResponseDto<>();
//    }

}
