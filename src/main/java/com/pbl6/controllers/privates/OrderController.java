package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.order.CreateOrderRequest;
import com.pbl6.dtos.request.order.SearchOrderRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.order.OrderDetailDto;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/private/order")
@RequiredArgsConstructor
@Tag(name = "Quản lý đơn hàng")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderController {

    final OrderService orderService;

    @PreAuthorize("hasAuthority('ORDER_CREATE')")
    @PostMapping("/create")
    @Operation(summary = "Tạo đơn hàng", security = { @SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> createOrder(@RequestBody CreateOrderRequest req) {
        orderService.createOrderManual(req);
        return new ApiResponseDto<>();
    }

    @PreAuthorize("hasAuthority('ORDER_READ')")
    @GetMapping("/list")
    @Operation(summary = "List đơn hàng", security = { @SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<PageDto<OrderDto>> listOrder(
            @ParameterObject SearchOrderRequest req
            ) {

        return new ApiResponseDto<>(orderService.searchOrders(req));
    }

    @PreAuthorize("hasAuthority('ORDER_READ')")
    @GetMapping("/detail/{orderId}")
    @Operation(summary = "Chi tiết đơn hàng", security = { @SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<OrderDetailDto> getOrderDetail(
            @PathVariable Long orderId
    ) {
        return new ApiResponseDto<>(orderService.getOrderDetail(orderId));
    }
    @PreAuthorize("hasAuthority('ORDER_UPDATE_STATUS')")
    @PutMapping("/confirm/{orderId}")
    @Operation(summary = "Xác nhận đơn hàng", security = { @SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<String> confirmOrder(
            @PathVariable Long orderId
    ) {
        orderService.confirmOrder(orderId);
        return new ApiResponseDto<>("Order confirmed successfully");
    }

    @PreAuthorize("hasAuthority('ORDER_UPDATE_STATUS')")
    @PutMapping("/cancel/{orderId}")
    @Operation(summary = "Huỷ đơn hàng", security = { @SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<String> cancelOrder(
            @PathVariable Long orderId
    ) {
        orderService.cancelOrder(orderId);
        return new ApiResponseDto<>("Order confirmed successfully");
    }
}
