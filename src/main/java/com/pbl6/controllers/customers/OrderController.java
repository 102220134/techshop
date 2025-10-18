package com.pbl6.controllers.customers;

import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.entities.UserEntity;
import com.pbl6.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("my-order")
    @Operation(summary = "Danh sách đơn hàng", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<List<OrderDto>> getOrderByUser() {
        Long userId = getCurrentUserId();
        ApiResponseDto<List<OrderDto>> response = new ApiResponseDto<>();
        response.setData(orderService.getOrderByUser(userId));
        return response;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return user.getId();
    }
}
