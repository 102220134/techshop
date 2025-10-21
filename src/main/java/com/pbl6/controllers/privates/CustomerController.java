package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.order.MyOrderRequest;
import com.pbl6.dtos.request.user.ChangePasswordRequest;
import com.pbl6.dtos.request.user.UserUpdateInfoRequest;
import com.pbl6.dtos.request.user.UserAddressCreateRequest;
import com.pbl6.dtos.request.user.UserAddressUpdateRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.dtos.user.UserAddressDto;
import com.pbl6.dtos.user.UserDetailDto;
import com.pbl6.services.OrderService;
import com.pbl6.services.UserService;
import com.pbl6.utils.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/customer/")
@RequiredArgsConstructor
@Tag(name = "Hồ sơ của tôi")
public class CustomerController {
    private final AuthenticationUtil authenticationUtil;
    private final UserService userService;
    private final OrderService orderService;

    @GetMapping("my-profile")
    @Operation(summary = "Lấy thong tin", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDetailDto> getProfile() {
        Long userId = authenticationUtil.getCurrentUserId();
        return new ApiResponseDto<>(userService.getUserInfo(userId));
    }

    @PutMapping("my-profile")
    @Operation(summary = "Cập nhật thông tin", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserDetailDto> updateProfile(@RequestBody UserUpdateInfoRequest req) {
        Long userId = authenticationUtil.getCurrentUserId();
        return new ApiResponseDto<>(userService.updateUserInfo(userId, req));
    }

    @PutMapping("change-password")
    @Operation(summary = "Đổi mật khẩu", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<?> changePassword(@RequestBody ChangePasswordRequest req) {
        Long userId = authenticationUtil.getCurrentUserId();
        userService.changePassword(userId, req);
        return new ApiResponseDto<>("Đổi mật khẩu thành công");
    }

    @PostMapping("my-address")
    @Operation(summary = "Thêm địa chỉ ", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserAddressDto> createUserAddress(@RequestBody UserAddressCreateRequest req) {
        Long userId = authenticationUtil.getCurrentUserId();
        return new ApiResponseDto<>(userService.createAddress(userId, req));
    }

    @PutMapping("my-address")
    @Operation(summary = "Cập nhật địa chỉ", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<UserAddressDto> updateUserAddress(@RequestBody UserAddressUpdateRequest req) {
        Long userId = authenticationUtil.getCurrentUserId();
        return new ApiResponseDto<>(userService.updateAddress(userId, req));
    }
    @DeleteMapping("my-address/{id}")
    @Operation(summary = "Xóa địa chỉ", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<?> deleteUserAddress(@PathVariable Long id) {
        Long userId = authenticationUtil.getCurrentUserId();
        userService.deleteAddress(userId, id);
        return new ApiResponseDto<>("Xóa địa chỉ thành công");
    }

    @GetMapping("my-order")
    @Operation(summary = "Đơn hàng của tôi", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<PageDto<OrderDto>> getOrderByUser(@ParameterObject MyOrderRequest request) {
        Long userId =  authenticationUtil.getCurrentUserId();
        return new ApiResponseDto<>(orderService.getOrderByUser(userId,request));
    }

}
