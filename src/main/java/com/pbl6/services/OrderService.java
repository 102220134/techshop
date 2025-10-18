package com.pbl6.services;

import com.pbl6.dtos.request.checkout.OrderRequest;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.entities.*;
import com.pbl6.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    void markOrderStatus(Long orderId,OrderStatus orderStatus);
    OrderEntity createOrder(OrderRequest req);
    void cancelOrderPaymentTimeout();
//    void cancelOrder(Long orderId);
    List<OrderDto> getOrderByUser(Long userID);
}
