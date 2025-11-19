package com.pbl6.services;

import com.pbl6.dtos.request.order.CreateOrderRequest;
import com.pbl6.dtos.request.order.MyOrderRequest;
import com.pbl6.dtos.request.order.SearchOrderRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.order.OrderDetailDto;
import com.pbl6.dtos.response.order.UserOrderDetailDto;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.entities.*;

public interface OrderService {
    OrderEntity createOrder(CreateOrderRequest req);
    OrderEntity createOrderManual(CreateOrderRequest req);
    void cancelOrderPaymentTimeout();
    PageDto<OrderDto> getOrderByUser(Long userId, MyOrderRequest request);
    PageDto<OrderDto> searchOrders(SearchOrderRequest req);
    UserOrderDetailDto getOrderDetailByUser(Long orderId);
    OrderDetailDto getOrderDetail(Long orderId);
    void confirmOrder(Long orderId);
    void cancelOrder(Long orderId);

    void startDelivery(Long orderId);

    void markAsDelivered(Long orderId);

    void completeOrder(Long orderId);

    void returnOrder(Long orderId);
}
