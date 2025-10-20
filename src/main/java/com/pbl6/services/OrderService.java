package com.pbl6.services;

import com.pbl6.dtos.request.order.OrderRequest;
import com.pbl6.dtos.request.order.MyOrderRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.entities.*;
import com.pbl6.enums.OrderStatus;

public interface OrderService {
    void markOrderStatus(Long orderId,OrderStatus orderStatus);
    OrderEntity createOrder(OrderRequest req);
    void cancelOrderPaymentTimeout();
//    void cancelOrder(Long orderId);
    PageDto<OrderDto> getOrderByUser(Long userId, MyOrderRequest request);
}
