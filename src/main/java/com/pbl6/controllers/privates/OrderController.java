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
// ================================================================
    // ===== CÁC ENDPOINT CẬP NHẬT TRẠNG THÁI (ĐÃ SỬA VÀ BỔ SUNG) =====
    // ================================================================

    @PreAuthorize("hasAuthority('ORDER_UPDATE_STATUS')")
    @PutMapping("/confirm/{orderId}")
    @Operation(summary = "Xác nhận đơn hàng (VD: PENDING -> CONFIRMED)", security = { @SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<String> confirmOrder(
            @PathVariable Long orderId
    ) {
        orderService.confirmOrder(orderId);
        return new ApiResponseDto<>("Order confirmed successfully");
    }

    @PreAuthorize("hasAuthority('ORDER_UPDATE_STATUS')")
    @PutMapping("/delivering/{orderId}")
    @Operation(summary = "Cập nhật Đang Giao Hàng (CONFIRMED -> DELIVERING)", security = { @SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<String> startDelivery(
            @PathVariable Long orderId
    ) {
        // Bạn cần implement logic này trong OrderService,
        // nó sẽ gọi sang DeliveryService.updateDeliveryStatus(..., DELIVERING)
        orderService.startDelivery(orderId);
        return new ApiResponseDto<>("Order status updated to DELIVERING");
    }

    @PreAuthorize("hasAuthority('ORDER_UPDATE_STATUS')")
    @PutMapping("/delivered/{orderId}")
    @Operation(summary = "Cập nhật Đã Giao Hàng (DELIVERING -> DELIVERED)", security = { @SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<String> markAsDelivered(
            @PathVariable Long orderId
    ) {
        // Tương tự, logic này trong OrderService sẽ gọi sang DeliveryService
        orderService.markAsDelivered(orderId);
        return new ApiResponseDto<>("Order status updated to DELIVERED");
    }

    @PreAuthorize("hasAuthority('ORDER_UPDATE_STATUS')")
    @PutMapping("/complete/{orderId}")
    @Operation(summary = "Hoàn thành đơn (DELIVERED -> COMPLETED)", security = { @SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<String> completeOrder(
            @PathVariable Long orderId
    ) {
        // Thường dùng sau khi đã đối soát thanh toán/COD
        orderService.completeOrder(orderId);
        return new ApiResponseDto<>("Order completed successfully");
    }

    @PreAuthorize("hasAuthority('ORDER_UPDATE_STATUS')")
    @PutMapping("/return/{orderId}")
    @Operation(summary = "Đơn hàng bị trả (VD: FAILED -> RETURNED)", security = { @SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<String> returnOrder(
            @PathVariable Long orderId
    ) {
        // Logic này sẽ gọi DeliveryService để hoàn kho
        orderService.returnOrder(orderId);
        return new ApiResponseDto<>("Order marked as RETURNED");
    }

    @PreAuthorize("hasAuthority('ORDER_UPDATE_STATUS')")
    @PutMapping("/cancel/{orderId}")
    @Operation(summary = "Huỷ đơn hàng (VD: PENDING -> CANCELLED)", security = { @SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<String> cancelOrder(
            @PathVariable Long orderId
    ) {
        orderService.cancelOrder(orderId);
        // FIX: Sửa lại nội dung response
        return new ApiResponseDto<>("Order cancelled successfully");
    }
}
