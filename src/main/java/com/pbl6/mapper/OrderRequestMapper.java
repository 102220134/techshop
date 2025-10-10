package com.pbl6.mapper;

import com.pbl6.dtos.request.checkout.CheckoutPickupRequest;
import com.pbl6.dtos.request.checkout.CheckoutShipRequest;
import com.pbl6.dtos.request.checkout.OrderItemRequest;
import com.pbl6.dtos.request.checkout.OrderRequest;
import com.pbl6.enums.ReceiveMethod;
import org.springframework.stereotype.Component;

@Component
public class OrderRequestMapper {
    public OrderRequest fromShip(CheckoutShipRequest req) {
        OrderRequest orderReq = new OrderRequest();
        orderReq.setReceiveMethod(ReceiveMethod.SHIPMENT);
        orderReq.setPaymentMethod(req.getPaymentMethod());
        orderReq.setFullName(req.getFullName());
        orderReq.setPhone(req.getPhone());
        orderReq.setEmail(req.getEmail());
        orderReq.setLine(req.getLine());
        orderReq.setWard(req.getWard());
        orderReq.setDistrict(req.getDistrict());
        orderReq.setProvince(req.getProvince());
        orderReq.setItems(req.getOrderItems().stream()
                .map(i -> new OrderItemRequest(i.getVariantId(), i.getQuantity()))
                .toList());
        return orderReq;
    }

    public OrderRequest fromPickup(CheckoutPickupRequest req) {
        OrderRequest orderReq = new OrderRequest();
        orderReq.setReceiveMethod(ReceiveMethod.RECEIVE_AT_STORE);
        orderReq.setPaymentMethod(req.getPaymentMethod());
        orderReq.setFullName(req.getFullName());
        orderReq.setPhone(req.getPhone());
        orderReq.setEmail(req.getEmail());
        orderReq.setStoreId(req.getStoreId());
        orderReq.setItems(req.getOrderItems().stream()
                .map(i -> new OrderItemRequest(i.getVariantId(), i.getQuantity()))
                .toList());
        return orderReq;
    }
}
