package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.order.MyOrderRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.entities.UserEntity;
import com.pbl6.services.OrderService;
import com.pbl6.utils.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/order")
@RequiredArgsConstructor
@Tag(name = "Quản lý đơn hàng")
public class OrderController {
    private final OrderService orderService;
    private final AuthenticationUtil authenticationUtil;

    @GetMapping("my-order")
    @Operation(summary = "Đơn hàng của tôi", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<PageDto<OrderDto>> getOrderByUser(@ParameterObject MyOrderRequest request) {
        Long userId =  authenticationUtil.getCurrentUserId();
        return new ApiResponseDto<>(orderService.getOrderByUser(userId,request));
    }
}
