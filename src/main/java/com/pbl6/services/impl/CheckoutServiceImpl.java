package com.pbl6.services.impl;


import com.pbl6.dtos.request.checkout.CheckoutPickupRequest;
import com.pbl6.dtos.request.checkout.CheckoutShipRequest;
import com.pbl6.dtos.request.order.CreateOrderRequest;
import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.entities.OrderEntity;
import com.pbl6.entities.StoreEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.enums.ReceiveMethod;
import com.pbl6.mapper.UserAddressMapper;
import com.pbl6.repositories.*;
import com.pbl6.services.*;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final UserRepository userRepo;
    private final VariantRepository variantRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final PaymentService paymentService;
    private final UserService userService;
    private final EntityUtil entityUtil;
    private final StoreRepository storeRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductSerialRepository productSerialRepository;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final UserAddressMapper userAddressMapper;

    @Override
    @Transactional
    public PaymentInitResponse processCheckoutShipment(UserEntity user, CheckoutShipRequest req) {

        CreateOrderRequest createOrderRequest = new CreateOrderRequest();

        createOrderRequest.setUserId(user.getId());
        createOrderRequest.setEmail(user.getEmail());
        createOrderRequest.setFullName(user.getName());
        createOrderRequest.setPhone(user.getPhone());
        createOrderRequest.setIsOnline(true);
        createOrderRequest.setNote(req.getNote());
        createOrderRequest.setPaymentMethod(req.getPaymentMethod());
        createOrderRequest.setReceiveMethod(ReceiveMethod.DELIVERY);

        createOrderRequest.setLine(req.getLine());
        createOrderRequest.setWard(req.getWard());
        createOrderRequest.setDistrict(req.getDistrict());
        createOrderRequest.setProvince(req.getProvince());

        createOrderRequest.setItems(req.getOrderItems());


        OrderEntity order = orderService.createOrder(createOrderRequest
        );

        //giữ hàng
        inventoryService.handleShip(order.getOrderItems());
        //Tạo yêu cầu payment
        PaymentInitResponse payRes = paymentService.create(order);

        return payRes;
    }

    @Override
    @Transactional
    public PaymentInitResponse processCheckoutPickup(UserEntity user, CheckoutPickupRequest req) {

        StoreEntity store = entityUtil.ensureExists(storeRepository.findById(req.getStoreId()));

        CreateOrderRequest createOrderRequest = new CreateOrderRequest();

        createOrderRequest.setUserId(user.getId());
        createOrderRequest.setEmail(user.getEmail());
        createOrderRequest.setFullName(user.getName());
        createOrderRequest.setPhone(user.getPhone());
        createOrderRequest.setIsOnline(true);
        createOrderRequest.setNote(req.getNote());
        createOrderRequest.setPaymentMethod(req.getPaymentMethod());
        createOrderRequest.setReceiveMethod(ReceiveMethod.PICKUP);

        createOrderRequest.setStoreId(store.getId());

        createOrderRequest.setItems(req.getOrderItems());
        //Tạo đơn hàng
        OrderEntity order = orderService.createOrder(createOrderRequest);

        //Giữ hàng
        inventoryService.handlePickupAtStore(store, order.getOrderItems());

        //Tạo yêu cầu payment
        PaymentInitResponse payRes = paymentService.create(order);

        return payRes;
    }

    private PaymentRequest toPaymentRequest(OrderEntity order) {
        return PaymentRequest.builder()
                .orderId(order.getId())
                .grossAmount(
                        order.getOrderItems().stream()
                                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                )
                .directDiscount(
                        order.getOrderItems().stream()
                                .map(item -> item.getDiscountAmount().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                )
                .voucherDiscount(order.getVoucherDiscount())
                .paymentMethod(order.getPaymentMethod())
                .receiveMethod(order.getReceiveMethod())
                .build();
    }

}
